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

package autosaveworld.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import autosaveworld.core.GlobalConstants;


public class AutoSaveWorldConfig {

	private FileConfiguration config;

	// some global variables
	public boolean varDebug = false;
	public boolean commandOnlyFromConsole = false;
	//save
	public int saveInterval = 900;
	public boolean saveBroadcast = true;
	public boolean saveEnabled = true;
	public boolean saveDisableStructureSaving = false;
	public boolean saveOnASWDisable = true;
	//backup
	public boolean backupEnabled = false;
	public int backupInterval =  60*60*6;
	public boolean backupBroadcast = true;
	public boolean backupsaveBefore = true;
	//localfs backup
	public boolean backupLFSEnabled = true;
	public List<String> backupLFSExtFolders;
	public List<String> backupLFSBackupWorldsList;
	public int backupLFSMaxNumberOfWorldsBackups = 15;
	public boolean backupLFSPluginsFolder = false;
	public int backupLFSMaxNumberOfPluginsBackups = 15;
	public List<String> backupLFSOtherFolders;
	public int backupLFSMaxNumberOfOtherBackups = 15;
	public List<String> backupLFSExcludeFolders;
	public boolean backupLFSZipEnabled = false;
	//ftp backup
	public boolean backupFTPEnabled = false;
	public String backupFTPHostname = "127.0.0.1";
	public int backupFTPPort = 21;
	public String backupFTPUsername = "user";
	public String backupFTPPassworld = "password";
	public String backupFTPPath = "asw";
	public List<String> backupFTPBackupWorldsList;
	public boolean backupFTPPluginsFolder = false;
	public List<String> backupFTPOtherFolders;
	public List<String> backupFTPExcludeFolders;
	public int backupFTPMaxNumberOfBackups = 4;
	public boolean backupFTPZipEnabled = false;
	//script
	public boolean backupScriptEnabled = false;
	public List<String> backupScriptPaths;
	//purge
	public boolean purgeEnabled = false;
	public int purgeInterval = 60*60*24;
	public long purgeAwayTime = 60*60*24*30;
	public List<String> purgeIgnoredNicks;
	public List<String> purgeIgnoredUUIDs;
	public boolean purgeBroadcast = true;
	public boolean purgeWERemoveUnsafe = false;
	public Set<String> purgeWERemoveUnsafeSafeIDs;
	public boolean purgeWG = true;
	public boolean purgeWGRegenRg = false;
	public boolean purgeWGNoregenOverlap = true;
	public boolean purgeLWC = true;
	public boolean purgeLWCDelProtectedBlocks = false;
	public boolean purgeMVInv = true;
	public boolean purgeResidence = true;
	public boolean purgeResidenceRegenArea = false;
	public boolean purgePerms = true;
	public String purgePermsSaveCMD = "mansave";
	public boolean purgeMyWarp = true;
	public boolean purgeDat = true;
	//crashrestart
	public boolean crashRestartEnabled = false;
	public boolean crashRestartForceStop = false;
	public String crashRestartScriptPath= "";
	public boolean crashRestartJustStop = false;
	public long crashRestartTimeout = 60;
	public int crashRestartCheckerStartDelay = 20;
	//autorestart
	public boolean autoRestart = false;
	public boolean autoRestartBroadcast = true;
	public String autoRestartScriptPath = "";
	public List<String> autoRestartTimes;
	public boolean autoRestartCountdown = true;
	public List<Integer> autoRestartCountdownSeconds;
	public List<String> autoRestartPreStopCommmands;
	public boolean autoRestartJustStop = false;
	//consolecmmand
	public boolean ccTimesModeEnabled = false;
	public Map<String, List<String>> ccTimesModeCommands;
	public boolean ccIntervalsModeEnabled = false;
	public Map<Integer, List<String>> ccIntervalsModeCommands;
	//worldregen
	public boolean worldRegenRemoveSeedData = false;
	public boolean worldRegenSaveWG = true;
	public boolean worldRegenSaveFactions = true;
	public boolean worldRegenSaveGP = true;
	public boolean worldregenSaveTowny = true;


	public void load() {

		config = YamlConfiguration.loadConfiguration(new File(GlobalConstants.getConfigPath()));

		//variables
		varDebug = config.getBoolean("var.debug", varDebug);
		commandOnlyFromConsole = config.getBoolean("var.commandsonlyfromconsole",commandOnlyFromConsole);

		//save variables
		saveEnabled = config.getBoolean("save.enabled",saveEnabled);
		saveDisableStructureSaving = config.getBoolean("save.disablestructuresaving", saveDisableStructureSaving);
		saveBroadcast = config.getBoolean("save.broadcast", saveBroadcast);
		saveInterval = config.getInt("save.interval", saveInterval);
		saveOnASWDisable = config.getBoolean("save.onplugindisable", saveOnASWDisable);

		//backup variables
		backupEnabled = config.getBoolean("backup.enabled", backupEnabled);
		backupInterval = config.getInt("backup.interval", backupInterval);
		backupBroadcast = config.getBoolean("backup.broadcast", backupBroadcast);
		backupsaveBefore = config.getBoolean("backup.savebefore",backupsaveBefore);
		//localfs
		backupLFSEnabled = config.getBoolean("backup.localfs.enabled",backupLFSEnabled);
		backupLFSMaxNumberOfWorldsBackups = config.getInt("backup.localfs.MaxNumberOfWorldsBackups", backupLFSMaxNumberOfWorldsBackups);
		backupLFSMaxNumberOfPluginsBackups = config.getInt("backup.localfs.MaxNumberOfPluginsBackups", backupLFSMaxNumberOfPluginsBackups);
		backupLFSExtFolders = config.getStringList("backup.localfs.destinationfolders");
		if (backupLFSExtFolders.isEmpty()) {
			backupLFSExtFolders.add(new File(".").getAbsolutePath());
		}
		backupLFSPluginsFolder = config.getBoolean("backup.localfs.pluginsfolder", backupLFSPluginsFolder);
		backupLFSExcludeFolders = config.getStringList("backup.localfs.excludefolders");
		backupLFSZipEnabled = config.getBoolean("backup.localfs.zip", backupLFSZipEnabled);
		backupLFSBackupWorldsList = config.getStringList("backup.localfs.worlds");
		if (backupLFSBackupWorldsList.size() == 0) {
			backupLFSBackupWorldsList.add("*");
		}
		backupLFSOtherFolders = config.getStringList("backup.localfs.otherfolders");
		backupLFSMaxNumberOfOtherBackups = config.getInt("backup.localfs.MaxNumberOfOtherFoldersBackups", backupLFSMaxNumberOfOtherBackups);
		//ftp
		backupFTPEnabled = config.getBoolean("backup.ftp.enabled",backupFTPEnabled);
		backupFTPHostname = config.getString("backup.ftp.hostname",backupFTPHostname);
		backupFTPPort = config.getInt("backup.ftp.port",backupFTPPort);
		backupFTPUsername = config.getString("backup.ftp.login",backupFTPUsername);
		backupFTPPassworld = config.getString("backup.ftp.password",backupFTPPassworld);
		backupFTPPath = config.getString("backup.ftp.path",backupFTPPath);
		backupFTPBackupWorldsList = config.getStringList("backup.ftp.worlds");
		backupFTPPluginsFolder = config.getBoolean("backup.ftp.pluginsfolder",backupFTPPluginsFolder);
		backupFTPOtherFolders = config.getStringList("backup.ftp.otherfolders");
		backupFTPExcludeFolders = config.getStringList("backup.ftp.excludefolders");
		backupFTPMaxNumberOfBackups = config.getInt("backup.ftp.maxNumberOfBackups",backupFTPMaxNumberOfBackups);
		backupFTPZipEnabled = config.getBoolean("backup.ftp.zip",backupFTPZipEnabled);
		if (backupFTPBackupWorldsList.size() == 0) {
			backupFTPBackupWorldsList.add("*");
		}
		//script
		backupScriptEnabled = config.getBoolean("backup.script.enabled",backupScriptEnabled);
		backupScriptPaths = config.getStringList("backup.script.scriptpaths");

		//purge variables
		purgeInterval = config.getInt("purge.interval", purgeInterval);
		purgeAwayTime = config.getLong("purge.awaytime", purgeAwayTime);
		purgeEnabled = config.getBoolean("purge.enabled", purgeEnabled);
		purgeIgnoredNicks = config.getStringList("purge.ignorednicks");
		purgeIgnoredUUIDs = config.getStringList("purge.ignoreduuids");
		purgeWERemoveUnsafe = config.getBoolean("purge.weregen.removeunsafeids", purgeWERemoveUnsafe);
		purgeWERemoveUnsafeSafeIDs = new HashSet<String>(config.getStringList("purge.weregen.safeids"));
		if (purgeWERemoveUnsafeSafeIDs.isEmpty()) {
			purgeWERemoveUnsafeSafeIDs.add("0-255");
		}
		purgeBroadcast = config.getBoolean("purge.broadcast", purgeBroadcast);
		purgeWG = config.getBoolean("purge.wg.enabled", purgeWG);
		purgeWGRegenRg = config.getBoolean("purge.wg.regenpurgedregion", purgeWGRegenRg);
		purgeWGNoregenOverlap = config.getBoolean("purge.wg.noregenoverlapregion",purgeWGNoregenOverlap);
		purgeLWC = config.getBoolean("purge.lwc.enabled", purgeLWC);
		purgeLWCDelProtectedBlocks = config.getBoolean("purge.lwc.deletepurgedblocks",purgeLWCDelProtectedBlocks);
		purgeMVInv = config.getBoolean("purge.mvinv.enabled",purgeMVInv);
		purgeResidence = config.getBoolean("purge.residence.enabled", purgeResidence);
		purgeResidenceRegenArea = config.getBoolean("purge.residence.regenpurgedresidence",purgeResidenceRegenArea);
		purgePerms = config.getBoolean("purge.permissions.enabled", purgePerms);
		purgePermsSaveCMD = config.getString("purge.permissions.savecmd", purgePermsSaveCMD);
		purgeMyWarp = config.getBoolean("purge.mywarp.enabled", purgeMyWarp);
		purgeDat = config.getBoolean("purge.dat.enabled", purgeDat);

		//crashrestart variables
		crashRestartEnabled = config.getBoolean("crashrestart.enabled", crashRestartEnabled);
		crashRestartForceStop = config.getBoolean("crashrestart.forcestop", crashRestartForceStop);
		crashRestartCheckerStartDelay = config.getInt("crashrestart.startdelay",crashRestartCheckerStartDelay);
		crashRestartScriptPath = config.getString("crashrestart.scriptpath",crashRestartScriptPath);
		crashRestartTimeout = config.getLong("crashrestart.timeout",crashRestartTimeout);
		crashRestartJustStop = config.getBoolean("crashrestart.juststop", crashRestartJustStop);

		//autorestart variables
		autoRestart = config.getBoolean("autorestart.enabled", autoRestart);
		autoRestartBroadcast = config.getBoolean("autorestart.broadcast", autoRestartBroadcast);
		autoRestartTimes = config.getStringList("autorestart.time");
		autoRestartCountdown = config.getBoolean("autorestart.countdown.enabled", autoRestartCountdown);
		autoRestartCountdownSeconds = config.getIntegerList("autorestart.countdown.broadcastonsecond");
		autoRestartPreStopCommmands = config.getStringList("autorestart.commands");
		autoRestartScriptPath = config.getString("autorestart.scriptpath",autoRestartScriptPath);
		autoRestartJustStop = config.getBoolean("autorestart.juststop", autoRestartJustStop);
		if (autoRestartCountdownSeconds.size() == 0) {
			autoRestartCountdownSeconds.add(60);
			autoRestartCountdownSeconds.add(30);
			for (int i = 1; i<=10; i++) {
				autoRestartCountdownSeconds.add(i);
			}
		}
		Collections.sort(autoRestartCountdownSeconds, Collections.reverseOrder());
		//autoconsolecommand variables
		ccTimesModeEnabled = config.getBoolean("consolecommand.timemode.enabled", ccTimesModeEnabled);
		HashMap<String, List<String>> cctimelmap = new HashMap<String, List<String>>();
		ConfigurationSection cctimescs = config.getConfigurationSection("consolecommand.timemode.times");
		if (cctimescs != null) {
			for (String time : cctimescs.getKeys(false)) {
				cctimelmap.put(time, cctimescs.getStringList(time));
			}
		}
		ccTimesModeCommands = cctimelmap;
		ccIntervalsModeEnabled = config.getBoolean("consolecommand.intervalmode.enabled", ccIntervalsModeEnabled);
		HashMap<Integer, List<String>> ccintervallmap = new HashMap<Integer, List<String>>();
		ConfigurationSection ccintervalscs = config.getConfigurationSection("consolecommand.intervalmode.intervals");
		if (ccintervalscs != null) {
			for (String interval : ccintervalscs.getKeys(false)) {
				try {
					ccintervallmap.put(Integer.valueOf(interval), ccintervalscs.getStringList(interval));
				} catch (Exception e) {
				}
			}
		}
		ccIntervalsModeCommands = ccintervallmap;

		//worldregen variables
		worldRegenRemoveSeedData = config.getBoolean("worldregen.newseed",worldRegenRemoveSeedData);
		worldRegenSaveWG = config.getBoolean("worldregen.savewg",worldRegenSaveWG);
		worldRegenSaveFactions = config.getBoolean("worldregen.savefactions",worldRegenSaveFactions);
		worldRegenSaveGP = config.getBoolean("worldregen.savegp",worldRegenSaveGP);
		worldregenSaveTowny = config.getBoolean("worldregen.savetowny",worldregenSaveTowny);

		save();
	}

	public void save() {
		config = new YamlConfiguration();


		// Variables
		config.set("var.debug", varDebug);
		config.set("var.commandsonlyfromconsole",commandOnlyFromConsole);

		//save variables
		config.set("save.enabled",saveEnabled);
		config.set("save.disablestructuresaving", saveDisableStructureSaving);
		config.set("save.interval", saveInterval);
		config.set("save.broadcast", saveBroadcast);
		config.set("save.onplugindisable", saveOnASWDisable);

		//backup variables
		config.set("backup.enabled", backupEnabled);
		config.set("backup.interval", backupInterval);
		config.set("backup.savebefore",backupsaveBefore);
		config.set("backup.broadcast", backupBroadcast);
		//localfs
		config.set("backup.localfs.enabled",backupLFSEnabled);
		config.set("backup.localfs.destinationfolders",backupLFSExtFolders);
		config.set("backup.localfs.zip",backupLFSZipEnabled);
		config.set("backup.localfs.worlds", backupLFSBackupWorldsList);
		config.set("backup.localfs.MaxNumberOfWorldsBackups", backupLFSMaxNumberOfWorldsBackups);
		config.set("backup.localfs.pluginsfolder", backupLFSPluginsFolder);
		config.set("backup.localfs.MaxNumberOfPluginsBackups", backupLFSMaxNumberOfPluginsBackups);
		config.set("backup.localfs.excludefolders",backupLFSExcludeFolders);
		config.set("backup.localfs.otherfolders", backupLFSOtherFolders);
		config.set("backup.localfs.MaxNumberOfOtherFoldersBackups", backupLFSMaxNumberOfOtherBackups);
		//ftp
		config.set("backup.ftp.enabled",backupFTPEnabled);
		config.set("backup.ftp.hostname",backupFTPHostname);
		config.set("backup.ftp.port",backupFTPPort);
		config.set("backup.ftp.login",backupFTPUsername);
		config.set("backup.ftp.password",backupFTPPassworld);
		config.set("backup.ftp.path",backupFTPPath);
		config.set("backup.ftp.zip",backupFTPZipEnabled);
		config.set("backup.ftp.worlds",backupFTPBackupWorldsList);
		config.set("backup.ftp.pluginsfolder",backupFTPPluginsFolder);
		config.set("backup.ftp.otherfolders",backupFTPOtherFolders);
		config.set("backup.ftp.excludefolders",backupFTPExcludeFolders);
		config.set("backup.ftp.maxNumberOfBackups",backupFTPMaxNumberOfBackups);
		//script
		config.set("backup.script.enabled",backupScriptEnabled);
		config.set("backup.script.scriptpaths",backupScriptPaths);


		//purge variables
		config.set("purge.enabled", purgeEnabled);
		config.set("purge.interval", purgeInterval);
		config.set("purge.awaytime", purgeAwayTime);
		config.set("purge.ignorednicks", purgeIgnoredNicks);
		config.set("purge.ignoreduuids", purgeIgnoredUUIDs);
		config.set("purge.broadcast",purgeBroadcast);
		config.set("purge.weregen.removeunsafeids", purgeWERemoveUnsafe);
		config.set("purge.weregen.safeids", new ArrayList<String>(purgeWERemoveUnsafeSafeIDs));
		config.set("purge.wg.enabled", purgeWG);
		config.set("purge.wg.regenpurgedregion", purgeWGRegenRg);
		config.set("purge.wg.noregenoverlapregion",purgeWGNoregenOverlap);
		config.set("purge.lwc.enabled", purgeLWC);
		config.set("purge.lwc.deletepurgedblocks", purgeLWCDelProtectedBlocks);
		config.set("purge.mvinv.enabled",purgeMVInv);
		config.set("purge.residence.enabled", purgeResidence);
		config.set("purge.residence.regenpurgedresidence",purgeResidenceRegenArea);
		config.set("purge.permissions.enabled", purgePerms);
		config.set("purge.permissions.savecmd", purgePermsSaveCMD);
		config.set("purge.mywarp.enabled", purgeMyWarp);
		config.set("purge.dat.enabled", purgeDat);

		//crashrestart variables
		config.set("crashrestart.enabled",crashRestartEnabled);
		config.set("crashrestart.forcestop", crashRestartForceStop);
		config.set("crashrestart.startdelay",crashRestartCheckerStartDelay);
		config.set("crashrestart.scriptpath",crashRestartScriptPath);
		config.set("crashrestart.timeout",crashRestartTimeout);
		config.set("crashrestart.juststop", crashRestartJustStop);

		//autorestart variables
		config.set("autorestart.enabled", autoRestart);
		config.set("autorestart.broadcast", autoRestartBroadcast);
		config.set("autorestart.time",autoRestartTimes);
		config.set("autorestart.countdown.enabled", autoRestartCountdown);
		config.set("autorestart.countdown.broadcastonsecond",autoRestartCountdownSeconds);
		config.set("autorestart.commands", autoRestartPreStopCommmands);
		config.set("autorestart.scriptpath",autoRestartScriptPath);
		config.set("autorestart.juststop", autoRestartJustStop);


		//autoconsolecommand variables
		config.set("consolecommand.timemode.enabled", ccTimesModeEnabled);
		if (ccTimesModeCommands.isEmpty()) {
			config.createSection("consolecommand.timemode.times");
		}
		for (String cctime : ccTimesModeCommands.keySet()) {
			config.set("consolecommand.timemode.times."+cctime, ccTimesModeCommands.get(cctime));
		}
		config.set("consolecommand.intervalmode.enabled", ccIntervalsModeEnabled);
		if (ccIntervalsModeCommands.isEmpty()) {
			config.createSection("consolecommand.intervalmode.intervals");
		}
		for (int inttime : ccIntervalsModeCommands.keySet()) {
			config.set("consolecommand.intervalmode.intervals."+inttime, ccIntervalsModeCommands.get(inttime));
		}

		//worldregen variables
		config.set("worldregen.newseed",worldRegenRemoveSeedData);
		config.set("worldregen.savewg",worldRegenSaveWG);
		config.set("worldregen.savefactions",worldRegenSaveFactions);
		config.set("worldregen.savegp",worldRegenSaveGP);
		config.set("worldregen.savetowny",worldregenSaveTowny);

		try {
			config.save(new File(GlobalConstants.getConfigPath()));
		} catch (IOException ex) {
		}
	}

}