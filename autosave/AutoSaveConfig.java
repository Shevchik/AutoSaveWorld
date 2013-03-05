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

package autosave;

import java.io.*;
import java.util.*;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;


public class AutoSaveConfig {
		
	private FileConfiguration config;
	public AutoSaveConfig(FileConfiguration config) {
		this.config = config;
	}
	
	
	// Variables
	protected UUID varUuid;
	protected int varInterval = 300;
	protected List<Integer> varWarnTimes = null;
	protected boolean varBroadcast = true;
	protected boolean varDebug = false;
	protected List<String> varWorlds = null;
	protected boolean savewarn = false;
	protected boolean backupEnabled = false;
	protected int backupInterval =  60*60*6;
	protected int MaxNumberOfWorldsBackups = 30;
	protected int MaxNumberOfPluginsBackups = 30;
	protected boolean backupBroadcast = true;
	protected boolean donotbackuptointfld = true;
	protected boolean backuppluginsfolder = false;
	protected boolean slowbackup = true;
	protected boolean backupwarn = false;
	protected List<Integer> backupWarnTimes = null;
	protected boolean backupzip = false;
	protected int purgeInterval = 60*60*24;
	protected long purgeAwayTime = 60*60*24*30;
	protected boolean purgeEnabled = false;
	protected boolean purgeBroadcast = true;
	protected boolean slowpurge = true;
	protected boolean wg = true;
	protected boolean wgregenrg = false;
	protected boolean lwc = true;
	protected boolean lwcdelprotectedblocks = false;
	protected boolean dat = true;
	protected boolean switchtolangfile = false;
	protected String langfileposfix = "ru";
	protected boolean crashrestartenabled = false;
	protected String crashrestartscriptpath="start.sh";
	protected long crtimeout = 30;
	protected boolean crdebug = false;
	
	
	protected List<String> extfolders;
	protected boolean backuptoextfolders = false;
	public void loadbackupextfolderconfig(){
		config = YamlConfiguration.loadConfiguration(new File("plugins/AutoSaveWorld/backupextfoldersconfig.yml"));
		extfolders = config.getStringList("extfolders");
		config = new YamlConfiguration();
		config.set("extfolders", extfolders);
		try {
			config.save(new File("plugins/AutoSaveWorld/backupextfoldersconfig.yml"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	//config load/save functions
	public void load() {
		
		config = YamlConfiguration.loadConfiguration(new File("plugins/AutoSaveWorld/config.yml"));
		
		// Variables
		varDebug = config.getBoolean("var.debug", varDebug);
		varUuid = UUID.fromString(config.getString("var.uuid", UUID.randomUUID().toString()));
		crdebug = config.getBoolean("var.crdebug", crdebug);
		
		
		
		//save variables
		varBroadcast = config.getBoolean("save.broadcast", varBroadcast);
		varInterval = config.getInt("save.interval", varInterval);
		savewarn = config.getBoolean("save.warn", savewarn);
		varWarnTimes = config.getIntegerList("save.warntime");
		if (varWarnTimes.size() == 0) {
			varWarnTimes.add(0);
			config.set("var.warntime", varWarnTimes);
		}

		//backup variables
		backupEnabled = config.getBoolean("backup.enabled", backupEnabled);
		backupInterval = config.getInt("backup.interval", backupInterval);
		slowbackup = config.getBoolean("backup.slowbackup", slowbackup);
		MaxNumberOfWorldsBackups = config.getInt("backup.MaxNumberOfWorldsBackups", MaxNumberOfWorldsBackups);
		MaxNumberOfPluginsBackups = config.getInt("backup.MaxNumberOfPluginsBackups", MaxNumberOfPluginsBackups);
		backupBroadcast = config.getBoolean("backup.broadcast", backupBroadcast);
		backuptoextfolders = config.getBoolean("backup.toextfolders", backuptoextfolders);
		donotbackuptointfld = config.getBoolean("backup.disableintfolder", donotbackuptointfld);
		backuppluginsfolder = config.getBoolean("backup.pluginsfolder", backuppluginsfolder);
		backupzip = config.getBoolean("backup.zip", backupzip);
		varWorlds = config.getStringList("backup.worlds");
		backupwarn = config.getBoolean("backup.warn", backupwarn);
		if (varWorlds.size() == 0) {
			varWorlds.add("*");
			config.set("var.worlds", varWorlds);
		}
		backupWarnTimes = config.getIntegerList("backup.warntime");
		if (backupWarnTimes.size() == 0) {
			backupWarnTimes.add(0);
			config.set("backup.warntime", backupWarnTimes);
		}
		
		//purge variables
		purgeInterval = config.getInt("purge.interval", purgeInterval);
		purgeAwayTime = config.getLong("purge.awaytime", purgeAwayTime);
		purgeEnabled = config.getBoolean("purge.enabled", purgeEnabled);
		purgeBroadcast = config.getBoolean("purge.broadcast", purgeBroadcast);
		slowpurge = config.getBoolean("purge.slowpurge", slowpurge);
		wg = config.getBoolean("purge.wg.enabled", wg);
		wgregenrg = config.getBoolean("purge.wg.regenpurgedregion", wgregenrg);
		lwc = config.getBoolean("purge.lwc.enabled", lwc);
		lwcdelprotectedblocks = config.getBoolean("purge.lwc.deletepurgedblocks",lwcdelprotectedblocks);
		dat = config.getBoolean("purge.dat.enabled", dat);
		
		//crashrestart variables
		crashrestartenabled = config.getBoolean("crashrestart.enabled",crashrestartenabled);
		crashrestartscriptpath = config.getString("crashrestart.scriptpath",crashrestartscriptpath);
		crtimeout = config.getLong("crashrestart.timeout",crtimeout);
		
		//locale variables
		switchtolangfile = config.getBoolean("locale.switchtolangfile",switchtolangfile);
		langfileposfix = config.getString("locale.langfilepostfix",langfileposfix);
		save();
	}

	public void save() {
		config = new YamlConfiguration();
		
				
		// Variables
		config.set("var.debug", varDebug);
		config.set("var.crdebug", crdebug);
		
		//save variables
		config.set("save.broadcast", varBroadcast);
		config.set("save.interval", varInterval);
		config.set("save.warn", savewarn);
		config.set("save.warntime", varWarnTimes);

		
		//backup variables
		config.set("backup.enabled", backupEnabled);
		config.set("backup.interval", backupInterval);
		config.set("backup.MaxNumberOfWorldsBackups", MaxNumberOfWorldsBackups);
		config.set("backup.pluginsfolder", backuppluginsfolder);
		config.set("backup.MaxNumberOfPluginsBackups", MaxNumberOfPluginsBackups);
		config.set("backup.broadcast", backupBroadcast);
		config.set("backup.toextfolders", backuptoextfolders);
		config.set("backup.disableintfolder", donotbackuptointfld);
		config.set("backup.zip",backupzip);
		config.set("backup.slowbackup", slowbackup);
		config.set("backup.worlds", varWorlds);
		config.set("backup.warn", backupwarn);
		config.set("backup.warntime", backupWarnTimes);
		
		//purge variables
		config.set("purge.interval", purgeInterval);
		config.set("purge.awaytime", purgeAwayTime);
		config.set("purge.enabled", purgeEnabled);
		config.set("purge.broadcast",purgeBroadcast);
		config.set("purge.slowpurge", slowpurge);
		config.set("purge.wg.enabled", wg);
		config.set("purge.wg.regenpurgedregion", wgregenrg);
		config.set("purge.lwc.enabled", lwc);
		config.set("purge.lwc.deletepurgedblocks", lwcdelprotectedblocks);
		config.set("purge.dat.enabled", dat);
		
		//crashrestart variables
		config.set("crashrestart.enabled",crashrestartenabled);
		config.set("crashrestart.scriptpath",crashrestartscriptpath);
		config.set("crashrestart.timeout",crtimeout);
		
		//locale variables
		config.set("locale.switchtolangfile",switchtolangfile);
		config.set("locale.langfilepostfix",langfileposfix);
		
		
		
						try {
					config.save(new File("plugins/AutoSaveWorld/config.yml"));
				} catch (IOException ex) {;
				}
	}
	
}