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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import autosaveworld.config.loader.Config;
import autosaveworld.config.loader.ConfigOption;
import autosaveworld.config.loader.postload.AstListAppend;
import autosaveworld.config.loader.postload.DefaultCountdown;
import autosaveworld.config.loader.postload.DefaultDestFolder;
import autosaveworld.config.loader.transform.ConfSectIntHashMap;
import autosaveworld.config.loader.transform.ConfSectStringHashMap;
import autosaveworld.config.loader.transform.ListClone;
import autosaveworld.core.GlobalConstants;

public class AutoSaveWorldConfig implements Config {

	// some global variables
	@ConfigOption(path = "var.debug")
	public boolean varDebug = false;
	@ConfigOption(path = "var.commandsonlyfromconsole")
	public boolean commandOnlyFromConsole = false;
	// save
	@ConfigOption(path = "save.interval")
	public int saveInterval = 900;
	@ConfigOption(path = "save.broadcast")
	public boolean saveBroadcast = true;
	@ConfigOption(path = "save.disablestructuresaving")
	public boolean saveDisableStructureSaving = false;
	@ConfigOption(path = "save.forceregioncachedump")
	public boolean saveDumpRegionCache = true;
	@ConfigOption(path = "save.onplugindisable")
	public boolean saveOnASWDisable = true;
	// backup
	@ConfigOption(path = "backup.enabled")
	public boolean backupEnabled = false;
	@ConfigOption(path = "backup.interval")
	public int backupInterval = 60 * 60 * 6;
	@ConfigOption(path = "backup.broadcast")
	public boolean backupBroadcast = true;
	@ConfigOption(path = "backup.savebefore")
	public boolean backupsaveBefore = true;
	@ConfigOption(path = "backup.rateLimit")
	public long backupRateLimit = -1;
	// localfs backup
	@ConfigOption(path = "backup.localfs.enabled")
	public boolean backupLFSEnabled = true;
	@ConfigOption(path = "backup.localfs.destinationfolders", transform = ListClone.class, postload = DefaultDestFolder.class)
	public List<String> backupLFSExtFolders = new ArrayList<>();
	@ConfigOption(path = "backup.localfs.worlds", transform = ListClone.class, postload = AstListAppend.class)
	public List<String> backupLFSBackupWorldsList = new ArrayList<>();
	@ConfigOption(path = "backup.localfs.MaxNumberOfWorldsBackups")
	public int backupLFSMaxNumberOfWorldsBackups = 15;
	@ConfigOption(path = "backup.localfs.pluginsfolder")
	public boolean backupLFSPluginsFolder = false;
	@ConfigOption(path = "backup.localfs.MaxNumberOfPluginsBackups")
	public int backupLFSMaxNumberOfPluginsBackups = 15;
	@ConfigOption(path = "backup.localfs.otherfolders", transform = ListClone.class)
	public List<String> backupLFSOtherFolders = new ArrayList<>();
	@ConfigOption(path = "backup.localfs.MaxNumberOfOtherFoldersBackups")
	public int backupLFSMaxNumberOfOtherBackups = 15;
	@ConfigOption(path = "backup.localfs.excludefolders", transform = ListClone.class)
	public List<String> backupLFSExcludeFolders = new ArrayList<>();
	@ConfigOption(path = "backup.localfs.zip")
	public boolean backupLFSZipEnabled = false;
	// ftp backup
	@ConfigOption(path = "backup.ftp.enabled")
	public boolean backupFTPEnabled = false;
	@ConfigOption(path = "backup.ftp.hostname")
	public String backupFTPHostname = "127.0.0.1";
	@ConfigOption(path = "backup.ftp.port")
	public int backupFTPPort = 21;
	@ConfigOption(path = "backup.ftp.login")
	public String backupFTPUsername = "user";
	@ConfigOption(path = "backup.ftp.password")
	public String backupFTPPassworld = "password";
	@ConfigOption(path = "backup.ftp.passive")
	public boolean backupFTPPassive = false;
	@ConfigOption(path = "backup.ftp.path")
	public String backupFTPPath = "asw";
	@ConfigOption(path = "backup.ftp.worlds", transform = ListClone.class, postload = AstListAppend.class)
	public List<String> backupFTPWorldsList = new ArrayList<>();
	@ConfigOption(path = "backup.ftp.pluginsfolder")
	public boolean backupFTPPluginsFolder = false;
	@ConfigOption(path = "backup.ftp.otherfolders", transform = ListClone.class)
	public List<String> backupFTPOtherFolders = new ArrayList<>();
	@ConfigOption(path = "backup.ftp.excludefolders", transform = ListClone.class)
	public List<String> backupFTPExcludeFolders = new ArrayList<>();
	@ConfigOption(path = "backup.ftp.maxNumberOfBackups")
	public int backupFTPMaxNumberOfBackups = 4;
	@ConfigOption(path = "backup.ftp.zip")
	public boolean backupFTPZipEnabled = false;
	// sftp backup
	@ConfigOption(path = "backup.sftp.enabled", legacypath = "backup.ftp.sftp")
	public boolean backupSFTPEnabled = false;
	@ConfigOption(path = "backup.sftp.hostname", legacypath = "backup.ftp.hostname")
	public String backupSFTPHostname = "127.0.0.1";
	@ConfigOption(path = "backup.sftp.port", legacypath = "backup.ftp.port")
	public int backupSFTPPort = 22;
	@ConfigOption(path = "backup.sftp.login", legacypath = "backup.ftp.login")
	public String backupSFTPUsername = "user";
	@ConfigOption(path = "backup.sftp.password", legacypath = "backup.ftp.password")
	public String backupSFTPPassworld = "password";
	@ConfigOption(path = "backup.sftp.path", legacypath = "backup.ftp.path")
	public String backupSFTPPath = "asw";
	@ConfigOption(path = "backup.sftp.worlds", legacypath = "backup.ftp.worlds", transform = ListClone.class, postload = AstListAppend.class)
	public List<String> backupSFTPWorldsList = new ArrayList<>();
	@ConfigOption(path = "backup.sftp.pluginsfolder", legacypath = "backup.ftp.pluginsfolder")
	public boolean backupSFTPPluginsFolder = false;
	@ConfigOption(path = "backup.sftp.otherfolders", legacypath = "backup.ftp.otherfolders", transform = ListClone.class)
	public List<String> backupSFTPOtherFolders = new ArrayList<>();
	@ConfigOption(path = "backup.sftp.excludefolders", legacypath = "backup.ftp.excludefolders", transform = ListClone.class)
	public List<String> backupSFTPExcludeFolders = new ArrayList<>();
	@ConfigOption(path = "backup.sftp.maxNumberOfBackups", legacypath = "backup.ftp.maxNumberOfBackups")
	public int backupSFTPMaxNumberOfBackups = 4;
	@ConfigOption(path = "backup.sftp.zip", legacypath = "backup.ftp.zip")
	public boolean backupSFTPZipEnabled = false;
	// script backup
	@ConfigOption(path = "backup.script.enabled")
	public boolean backupScriptEnabled = false;
	@ConfigOption(path = "backup.script.scriptpaths", transform = ListClone.class)
	public List<String> backupScriptPaths = new ArrayList<>();
	// dropbox backup
	@ConfigOption(path = "backup.dropbox.enabled")
	public boolean backupDropboxEnabled = false;
	@ConfigOption(path = "backup.dropbox.token")
	public String backupDropboxAPPTOKEN = "";
	@ConfigOption(path = "backup.dropbox.path")
	public String backupDropboxPath = "asw";
	@ConfigOption(path = "backup.dropbox.worlds", transform = ListClone.class, postload = AstListAppend.class)
	public List<String> backupDropboxWorldsList = new ArrayList<>();
	@ConfigOption(path = "backup.dropbox.pluginsfolder")
	public boolean backupDropboxPluginsFolder = false;
	@ConfigOption(path = "backup.dropbox.otherfolders", transform = ListClone.class)
	public List<String> backupDropboxOtherFolders = new ArrayList<>();
	@ConfigOption(path = "backup.dropbox.excludefolders", transform = ListClone.class)
	public List<String> backupDropboxExcludeFolders = new ArrayList<>();
	@ConfigOption(path = "backup.dropbox.maxNumberOfBackups")
	public int backupDropboxMaxNumberOfBackups = 4;
	@ConfigOption(path = "backup.dropbox.zip")
	public boolean backupDropboxZipEnabled = false;
	// google drive backup
	@ConfigOption(path = "backup.googledrive.enabled")
	public boolean backupGDriveEnabled = false;
	@ConfigOption(path = "backup.googledrive.authfilepath")
	public String backupGDriveAuthFile = "";
	@ConfigOption(path = "backup.googledrive.rootfolderid")
	public String backupGDRiveRootFolder = "";
	@ConfigOption(path = "backup.googledrive.path")
	public String backupGDrivePath = "asw";
	@ConfigOption(path = "backup.googledrive.worlds", transform = ListClone.class, postload = AstListAppend.class)
	public List<String> backupGDriveWorldsList = new ArrayList<>();
	@ConfigOption(path = "backup.googledrive.pluginsfolder")
	public boolean backupGDrivePluginsFolder = false;
	@ConfigOption(path = "backup.googledrive.otherfolders", transform = ListClone.class)
	public List<String> backupGDriveOtherFolders = new ArrayList<>();
	@ConfigOption(path = "backup.googledrive.excludefolders", transform = ListClone.class)
	public List<String> backupGDriveExcludeFolders = new ArrayList<>();
	@ConfigOption(path = "backup.googledrive.maxNumberOfBackups")
	public int backupGDriveMaxNumberOfBackups = 4;
	@ConfigOption(path = "backup.googledrive.zip")
	public boolean backupGDriveZipEnabled = false;
	// purge
	@ConfigOption(path = "purge.enabled")
	public boolean purgeEnabled = false;
	@ConfigOption(path = "purge.interval")
	public int purgeInterval = 60 * 60 * 24;
	@ConfigOption(path = "purge.awaytime")
	public long purgeAwayTime = 60 * 60 * 24 * 30;
	@ConfigOption(path = "purge.ignorednicks", transform = ListClone.class)
	public List<String> purgeIgnoredNicks = new ArrayList<>();
	@ConfigOption(path = "purge.ignoreduuids", transform = ListClone.class)
	public List<String> purgeIgnoredUUIDs = new ArrayList<>();
	@ConfigOption(path = "purge.broadcast")
	public boolean purgeBroadcast = true;
	@ConfigOption(path = "purge.wg.enabled")
	public boolean purgeWG = true;
	@ConfigOption(path = "purge.wg.regenpurgedregion")
	public boolean purgeWGRegenRg = false;
	@ConfigOption(path = "purge.wg.noregenoverlapregion")
	public boolean purgeWGNoregenOverlap = true;
	@ConfigOption(path = "purge.lwc.enabled")
	public boolean purgeLWC = true;
	@ConfigOption(path = "purge.lwc.deletepurgedblocks")
	public boolean purgeLWCDelProtectedBlocks = false;
	@ConfigOption(path = "purge.permissions.enabled")
	public boolean purgePerms = true;
	@ConfigOption(path = "purge.permissions.savecmd")
	public String purgePermsSaveCMD = "mansave force";
	@ConfigOption(path = "purge.mywarp.enabled")
	public boolean purgeMyWarp = true;
	@ConfigOption(path = "purge.essentials.enabled")
	public boolean purgeEssentials = true;
	@ConfigOption(path = "purge.dat.enabled")
	public boolean purgeDat = true;
	// restart
	@ConfigOption(path = "restart.juststop")
	public boolean restartJustStop = false;
	@ConfigOption(path = "restart.oncrash.enabled", legacypath = "crashrestart.enabled")
	public boolean restartOncrashEnabled = false;
	@ConfigOption(path = "restart.oncrash.scriptpath", legacypath = "crashrestart.scriptpath")
	public String restartOnCrashScriptPath = "";
	@ConfigOption(path = "restart.oncrash.timeout", legacypath = "crashrestart.timeout")
	public long restartOnCrashTimeout = 60;
	@ConfigOption(path = "restart.oncrash.checkerstartdelay", legacypath = "crashrestart.startdelay")
	public int restartOnCrashCheckerStartDelay = 20;
	@ConfigOption(path = "restart.oncrash.runonnonpluginstop", legacypath = "crashrestart.runonnonpluginstop")
	public boolean restartOnCrashOnNonAswStop = false;
	@ConfigOption(path = "restart.auto.enabled", legacypath = "autorestart.enabled")
	public boolean autoRestart = false;
	@ConfigOption(path = "restart.auto.broadcast", legacypath = "autorestart.broadcast")
	public boolean autoRestartBroadcast = true;
	@ConfigOption(path = "restart.auto.scriptpath", legacypath = "autorestart.scriptpath")
	public String autoRestartScriptPath = "";
	@ConfigOption(path = "restart.auto.time", legacypath = "autorestart.time")
	public List<String> autoRestartTimes = new ArrayList<>();
	@ConfigOption(path = "restart.auto.countdown.enabled", legacypath = "autorestart.countdown.enabled")
	public boolean autoRestartCountdown = true;
	@ConfigOption(path = "restart.auto.countdown.broadcastonsecond", legacypath = "autorestart.countdown.broadcastonsecond", transform = ListClone.class, postload = DefaultCountdown.class)
	public List<Integer> autoRestartCountdownSeconds = new ArrayList<>();
	@ConfigOption(path = "restart.auto.commands", transform = ListClone.class, legacypath = "autorestart.commands")
	public List<String> autoRestartPreStopCommmands = new ArrayList<>();
	// consolecmmand
	@ConfigOption(path = "consolecommand.timemode.enabled")
	public boolean ccTimesModeEnabled = false;
	@ConfigOption(path = "consolecommand.timemode.times", transform = ConfSectStringHashMap.class)
	public Map<String, List<String>> ccTimesModeCommands = new HashMap<>();
	@ConfigOption(path = "consolecommand.intervalmode.enabled")
	public boolean ccIntervalsModeEnabled = false;
	@ConfigOption(path = "consolecommand.intervalmode.intervals", transform = ConfSectIntHashMap.class)
	public Map<Integer, List<String>> ccIntervalsModeCommands = new HashMap<>();
	// worldregen
	@ConfigOption(path = "worldregen.newseed")
	public boolean worldRegenRemoveSeedData = false;
	@ConfigOption(path = "worldregen.preserveradius")
	public int worldRegenPreserveRadius = 0;
	@ConfigOption(path = "worldregen.savewg")
	public boolean worldRegenSaveWG = true;
	@ConfigOption(path = "worldregen.savefactions")
	public boolean worldRegenSaveFactions = true;
	@ConfigOption(path = "worldregen.savegp")
	public boolean worldRegenSaveGP = true;
	@ConfigOption(path = "worldregen.savetowny")
	public boolean worldregenSaveTowny = true;
	@ConfigOption(path = "worldregen.savepstones")
	public boolean worldregenSavePStones = true;
	// network watcher
	@ConfigOption(path = "networkwatcher.mainthreadnetaccess.warn")
	public boolean networkWatcherWarnMainThreadAcc = true;
	@ConfigOption(path = "networkwatcher.mainthreadnetaccess.interrupt")
	public boolean networkWatcherInterruptMainThreadNetAcc = false;

	@Override
	public File getFile() {
		return GlobalConstants.getMainConfigPath();
	}

}