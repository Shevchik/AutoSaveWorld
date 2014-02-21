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

package autosaveworld.threads.backup;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import autosaveworld.config.AutoSaveConfig;
import autosaveworld.config.AutoSaveConfigMSG;
import autosaveworld.core.AutoSaveWorld;
import autosaveworld.threads.backup.ftp.FTPBackup;
import autosaveworld.threads.backup.localfs.LocalFSBackup;
import autosaveworld.threads.backup.script.ScriptBackup;

public class AutoBackupThread extends Thread {

	private AutoSaveWorld plugin = null;
	private AutoSaveConfig config;
	private AutoSaveConfigMSG configmsg;
	public AutoBackupThread(AutoSaveWorld plugin, AutoSaveConfig config, AutoSaveConfigMSG configmsg) {
		this.plugin = plugin;
		this.config = config;
		this.configmsg = configmsg;
	}


	public void stopThread() {
		//save counter on disable
		if (config.backupEnabled)
		{
			FileConfiguration config = new YamlConfiguration();
			config.set("counter", counter);
			try {config.save(new File(plugin.constants.getBackupIntervalPreservePath()));} catch (IOException e) {}
		}
		//stop
		run = false;
	}

	public void startbackup() {
		if (plugin.backupInProgress) {
			plugin.warn("Multiple concurrent backups attempted! Backup interval is likely too short!");
			return;
		}
		command = true;
	}


	// The code to run...weee
	private volatile boolean run = true;
	private boolean command = false;
	private int counter = 0;
	@Override
	public void run() {

		plugin.debug("AutoBackupThread Started");
		Thread.currentThread().setName("AutoSaveWorld AutoBackupThread");

		//load counter on enable
		if (config.backupEnabled)
		{
			File preservefile = new File(plugin.constants.getBackupIntervalPreservePath());
			FileConfiguration config = YamlConfiguration.loadConfiguration(preservefile);
			counter = config.getInt("counter",0);
			preservefile.delete();
		}

		while (run) {
			// Prevent AutoBackup from never sleeping
			// If interval is 0, sleep for 10 seconds and skip backup
			if(config.backupInterval == 0) {
				try {Thread.sleep(10000);} catch(InterruptedException e) {}
				continue;
			}

			// Do our Sleep stuff!
			for (; counter < config.backupInterval; counter++) {
				if (!run || command) {break;}
				try {Thread.sleep(1000);} catch (InterruptedException e) {}
			}

			counter = 0;
			if (run&&(config.backupEnabled||command)) {performBackup();}

		}

		plugin.debug("Graceful quit of AutoBackupThread");

	}


	public void performBackup()
	{
		command = false;

		if (plugin.purgeInProgress) {
			plugin.warn("AutoPurge is in progress. Backup cancelled.");
			return;
		}
		if (plugin.saveInProgress) {
			plugin.warn("AutoSave is in progress. Backup cancelled.");
			return;
		}
		if (plugin.worldregenInProcess) {
			plugin.warn("WorldRegen is in progress. Backup cancelled.");
			return;
		}

		if (config.backupsaveBefore)
		{
			plugin.saveThread.performSave(true);
		}

		// Lock
		plugin.backupInProgress = true;

		long timestart = System.currentTimeMillis();

		plugin.broadcast(configmsg.messageBackupBroadcastPre, config.backupBroadcast);

		if (config.localfsbackupenabled)
		{
			plugin.debug("Starting LocalFS backup");
			new LocalFSBackup(plugin, config).performBackup();
			plugin.debug("LocalFS backup finished");
		}

		if (config.ftpbackupenabled)
		{
			plugin.debug("Starting FTP backup");
			new FTPBackup(plugin, config).performBackup();
			plugin.debug("FTP backup finished");
		}

		if (config.scriptbackupenabled)
		{
			plugin.debug("Starting Script Backup");
			new ScriptBackup(plugin, config).performBackup();
			plugin.debug("Script Backup Finished");
		}

		plugin.debug("Full backup time: "+(System.currentTimeMillis()-timestart)+" milliseconds");

		plugin.broadcast(configmsg.messageBackupBroadcastPost, config.backupBroadcast);

		// Release
		plugin.backupInProgress = false;
	}

}




