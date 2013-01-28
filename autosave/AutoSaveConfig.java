/**
 * 
 * Copyright 2011 MilkBowl (https://github.com/MilkBowl)
 * Copyright 2012 Shevchik
 * 
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
	
	protected String extpath;
	protected boolean backupEnabled = false;
	protected int backupInterval =  60*60*6;
	protected int MaxNumberOfBackups = 30;
	protected boolean backupBroadcast = true;
	protected boolean donotbackuptointfld = true;
	protected boolean backuppluginsfolder = false;
	protected boolean slowbackup = false;
	protected boolean backupwarn = false;
	protected List<Integer> backupWarnTimes = null;
	protected boolean backupzip = false;
	protected int purgeInterval = 60*60*24;
	protected long purgeAwayTime = 60*60*24*30;
	protected boolean purgeEnabled = false;
	protected boolean purgeBroadcast = true;
	
	
	protected List<String> extfolders;
	protected boolean backuptoextfolders = false;
	public void loadbackupextfolderconfig(){
		config = YamlConfiguration.loadConfiguration(new File("plugins/AutoSaveWorld/backupextfoldersconfig.yml"));
		extfolders = config.getStringList("extfolders");
		config = new YamlConfiguration();
		config.set("help", "write absolute paths to this file");
		config.set("extfolders", extfolders);
		try {
			config.save(new File("plugins/AutoSaveWorld/backupextfoldersconfig.yml"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void savebackupextfolderconfig() {
		config = new YamlConfiguration();
		config.set("help", "write absolute paths to this file");
		config.set("extfolders", extfolders);
		try {
			config.save(new File("plugins/AutoSaveWorld/backupextfoldersconfig.yml"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void load() {
		
		config = YamlConfiguration.loadConfiguration(new File("plugins/AutoSaveWorld/config.yml"));
		
		// Variables
		varDebug = config.getBoolean("var.debug", varDebug);
		varUuid = UUID.fromString(config.getString("var.uuid", UUID.randomUUID().toString()));
		
		
		
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
		
		MaxNumberOfBackups = config.getInt("backup.MaxNumberOfBackups", 30);
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
		save();
	}
	protected long datesec;
	public void getbackupdate() {
		config = new YamlConfiguration();
		String dt=new java.text.SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(java.util.Calendar.getInstance ().getTime());
		config.set("Backuped at: ",dt);
		try {
			config.save(new File("backups"+File.separator+datesec+File.separator+"backupinfo.yml"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
	
	public void getbackupdateext() {
		String dt=new java.text.SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(java.util.Calendar.getInstance ().getTime());
		config.set("Backuped at: ",dt);
		try {
			config.save(new File(extpath+File.separator+"backups"+File.separator+datesec+File.separator+"backupinfo.yml"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}

	protected int numberofbackups = 0;
	protected List<Long> backupnames;
	public void loadConfigBackup(){
		config = YamlConfiguration.loadConfiguration(new File("backups"+File.separator+"backups.yml"));
		numberofbackups = config.getInt("NOB", 0);
		backupnames = config.getLongList("listnames");
		
	}
	
	protected int numberofbackupsext = 0;
	protected List<Long> backupnamesext;
	public void loadConfigBackupExt(){
		config = YamlConfiguration.loadConfiguration(new File(extpath+File.separator+"backups.yml"));
		numberofbackupsext = config.getInt("NOB", 0);
		backupnamesext = config.getLongList("listnames");
		
	}
	
	
	public void saveConfigBackupExt(){
		config = new YamlConfiguration();
		config.set("NOB", numberofbackupsext);
		config.set("listnames", backupnamesext);
		try {
			config.save(new File(extpath+File.separator+"backups.yml"));
		} catch (IOException e) {
		}
	}
	
	public void saveConfigBackup(){
		config = new YamlConfiguration();
		config.set("NOB", numberofbackups);
		config.set("listnames", backupnames);
		try {
			config.save(new File("backups"+File.separator+"backups.yml"));
		} catch (IOException e) {
		}
	}
	
	
	public void save() {
		config = new YamlConfiguration();
		
				
		// Variables
		config.set("var.debug", varDebug);
		
		//save variables
		config.set("save.broadcast", varBroadcast);
		config.set("save.interval", varInterval);
		config.set("save.warn", savewarn);
		config.set("save.warntime", varWarnTimes);

		
		//backup variables
		config.set("backup.enabled", backupEnabled);
		config.set("backup.interval", backupInterval);
		config.set("backup.MaxNumberOfBackups", MaxNumberOfBackups);
		config.set("backup.broadcast", backupBroadcast);
		config.set("backup.toextfolders", backuptoextfolders);
		config.set("backup.disableintfolder", donotbackuptointfld);
		config.set("backup.pluginsfolder", backuppluginsfolder);
		config.set("backup.slowbackup", slowbackup);
		config.set("backup.worlds", varWorlds);
		config.set("backup.warn", backupwarn);
		config.set("backup.zip",backupzip);
		config.set("backup.warntime", backupWarnTimes);
		
		//purge variables
		config.set("purge.interval", purgeInterval);
		config.set("purge.awaytime", purgeAwayTime);
		config.set("purge.enabled", purgeEnabled);
		config.set("purge.broadcast",purgeBroadcast);

		
						try {
					config.save(new File("plugins/AutoSaveWorld/config.yml"));
				} catch (IOException ex) {;
				}
	}
}