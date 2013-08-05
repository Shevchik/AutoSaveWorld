/**
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * 
 */

package autosaveworld.threads.backup.localfs;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import autosaveworld.config.AutoSaveConfig;
import autosaveworld.core.AutoSaveWorld;

public class LocalFSBackup {
	
    private FileConfiguration configbackup;


    
    private AutoSaveWorld plugin;
    private AutoSaveConfig config;
    public LocalFSBackup(AutoSaveWorld plugin, AutoSaveConfig config)
    {
    	this.plugin = plugin;
    	this.config = config;
    }
    
	
	private int numberofbackupsw = 0;
	private List<Long> backupnamesw;
	private int numberofbackupspl = 0;
	private List<Long> backupnamespl;
	private void loadConfigBackupExt(String extpath){
		configbackup = YamlConfiguration.loadConfiguration(new File(extpath+File.separator+"backups"+File.separator+"backups.yml"));
		numberofbackupsw = configbackup.getInt("worlds.numberofbackups", 0);
		backupnamesw = configbackup.getLongList("worlds.listnames");
		numberofbackupspl = configbackup.getInt("plugins.numberofbackups", 0);
		backupnamespl = configbackup.getLongList("plugins.listnames");
	}
	
	
	private void saveConfigBackupExt(String extpath){
		configbackup = new YamlConfiguration();
		configbackup.set("worlds.numberofbackups", numberofbackupsw);
		configbackup.set("worlds.listnames", backupnamesw);
		configbackup.set("plugins.numberofbackups", numberofbackupspl);
		configbackup.set("plugins.listnames", backupnamespl);
		try {
			configbackup.save(new File(extpath+File.separator+"backups"+File.separator+"backups.yml"));
		} catch (IOException e) {
		}
	}
	
	
	public void performBackup() {

		boolean zip = config.lfsbackupzip;
	    List<String> backupfoldersdest = new ArrayList<String>();

		//adding internal folder to list of folders to which we should backup everything 
		if (!(config.lfsdonotbackuptointfld && config.lfsbackuptoextfolders))  {
			try {
				backupfoldersdest.add(new File(".").getCanonicalPath());
			} catch (IOException e) {e.printStackTrace();}
		}
		//adding external folders to list of folders to which we should backup everything 
		if (config.lfsbackuptoextfolders) {backupfoldersdest.addAll(config.lfsextfolders);}
		
		//backup	
		for (String extpath : backupfoldersdest)
		{
			//load backup operations class
			BackupOperations bo = new BackupOperations(plugin, zip, extpath, config.lfsbackupexcludefolders);
			
			//load info about backups stored in file backups.yml
			loadConfigBackupExt(extpath);

			//start worlds backup
			
			//delete oldest worlds backup if needed
			if (!(config.lfsMaxNumberOfWorldsBackups == 0) && (numberofbackupsw >= config.lfsMaxNumberOfWorldsBackups)) 
			{
				plugin.debug("Deleting oldest worlds backup");
				bo.deleteOldestWorldBackup(new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(backupnamesw.get(0)));
				backupnamesw.remove(0);
				numberofbackupsw--;
			}
			
			//do worlds backup
			plugin.debug("Backuping Worlds");
			//create list of worlds that we need to backup
			List<String> worldstobackup = new ArrayList<String>();
			if ((config.lfsbackupWorldsList).contains("*")) {
				for (World w : Bukkit.getWorlds()) {
					worldstobackup.add(w.getWorldFolder().getName());
				}
			} else {
				worldstobackup = config.lfsbackupWorldsList;
			}
			bo.backupWorlds(worldstobackup);
			plugin.debug("Backuped Worlds");
			backupnamesw.add(plugin.backupThread6.datesec);
			numberofbackupsw++;
			
			
			//now do plugins backup
			if (config.lfsbackuppluginsfolder) {
				
				//remove oldest plugins backup
				if (!(config.lfsMaxNumberOfPluginsBackups == 0) && (numberofbackupspl >= config.lfsMaxNumberOfPluginsBackups)) {
					plugin.debug("Deleting oldest plugins backup");
					bo.deleteOldestPluginsBackup(new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(backupnamespl.get(0)));
					backupnamespl.remove(0);
					numberofbackupspl--;
				}	
				
				//do plugins backup
				plugin.debug("Backuping plugins");
				bo.backupPlugins();
				plugin.debug("Backuped plugins");
				backupnamespl.add(plugin.backupThread6.datesec);
				numberofbackupspl++;
				
			}
			
			//save info about backups
			saveConfigBackupExt(extpath);
		}
	}
	
	
}
