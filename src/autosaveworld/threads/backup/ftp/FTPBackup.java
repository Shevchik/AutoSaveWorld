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

package autosaveworld.threads.backup.ftp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.bukkit.Bukkit;
import org.bukkit.World;

import autosaveworld.config.AutoSaveConfig;
import autosaveworld.core.AutoSaveWorld;
import autosaveworld.threads.backup.ftp.FTPBackupOperations;

public class FTPBackup {
    
	
    private AutoSaveWorld plugin;
    private AutoSaveConfig config;
    public FTPBackup(AutoSaveWorld plugin, AutoSaveConfig config)
    {
    	this.plugin = plugin;
    	this.config = config;
    }
    
    
    
    
    
    public void performBackup()
    {
    	FTPClient ftpclient = new FTPClient();
    	try {
    		//connect
			ftpclient.connect(config.ftphostname, config.ftpport);
		    if (!FTPReply.isPositiveCompletion(ftpclient.getReplyCode())) {
		    	ftpclient.disconnect();
		        Bukkit.getLogger().severe("[AutoSaveWorld] Failed to connect to ftp server. Backup to ftp server failed");
		    }
	    	ftpclient.login(config.ftpusername, config.ftppassworld);
	    	//create dirs
	    	ftpclient.makeDirectory(config.ftppath);
	    	ftpclient.changeWorkingDirectory(config.ftppath);
	    	ftpclient.makeDirectory("backups");
	    	ftpclient.changeWorkingDirectory("backups");
	    	String datedir = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(plugin.backupThread6.datesec);
	    	ftpclient.makeDirectory(datedir);
	    	ftpclient.changeWorkingDirectory(datedir);
	        ftpclient.setFileType(FTPClient.BINARY_FILE_TYPE);
	        //load BackupOperations class
			FTPBackupOperations bo = new FTPBackupOperations(plugin, ftpclient, config.ftpbackupzip, config.ftpbackupexcludefolders);
			//do worlds backup
			plugin.debug("Backuping Worlds");
			//create list of worlds that we need to backup
			List<String> worldstobackup = new ArrayList<String>();
			if ((config.ftpbackupWorlds).contains("*")) {
				for (World w : Bukkit.getWorlds()) {
					worldstobackup.add(w.getWorldFolder().getName());
				}
			} else {
				worldstobackup = config.ftpbackupWorlds;
			}
			bo.backupWorlds(worldstobackup);
			plugin.debug("Backuped Worlds");
			//do plugins backup
			if (config.ftpbackuppluginsfolder)
			{
				plugin.debug("Backuping plugins");
				bo.backupPlugins();
				plugin.debug("Backuped plugins");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
    	
    }

        
}
