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

import autosaveworld.config.AutoSaveWorldConfig;
import autosaveworld.config.AutoSaveWorldConfigMSG;
import autosaveworld.core.AutoSaveWorld;
import autosaveworld.core.GlobalConstants;
import autosaveworld.core.logging.MessageLogger;
import autosaveworld.threads.backup.dropbox.DropboxBackup;
import autosaveworld.threads.backup.ftp.FTPBackup;
import autosaveworld.threads.backup.localfs.LocalFSBackup;
import autosaveworld.threads.backup.script.ScriptBackup;
import autosaveworld.threads.backup.sftp.SFTPBackup;

public class AutoBackupThread extends Thread {

	private AutoSaveWorld plugin = null;
	private AutoSaveWorldConfig config;
	private AutoSaveWorldConfigMSG configmsg;
	public AutoBackupThread(AutoSaveWorld plugin, AutoSaveWorldConfig config, AutoSaveWorldConfigMSG configmsg) {
		this.plugin = plugin;
		this.config = config;
		this.configmsg = configmsg;
	}


	public void stopThread() {
		//save counter on disable
		if (config.backupEnabled) {
			FileConfiguration config = new YamlConfiguration();
			config.set("counter", counter);
			try {config.save(new File(GlobalConstants.getBackupIntervalPreservePath()));} catch (IOException e) {}
		}
		//stop
		run = false;
	}

	public void startbackup() {
		command = true;
	}


	// The code to run...weee
	private volatile boolean run = true;
	private boolean command = false;
	private int counter = 0;
	@Override
	public void run() {

		MessageLogger.debug("AutoBackupThread Started");
		Thread.currentThread().setName("AutoSaveWorld AutoBackupThread");

		//load counter on enable
		if (config.backupEnabled){
			File preservefile = new File(GlobalConstants.getBackupIntervalPreservePath());
			FileConfiguration config = YamlConfiguration.loadConfiguration(preservefile);
			counter = config.getInt("counter", 0);
			preservefile.delete();
		}

		while (run) {
			// Do our Sleep stuff!
			for (; counter < config.backupInterval; counter++) {
				if (!run || command) {break;}
				try {Thread.sleep(1000);} catch (InterruptedException e) {}
			}

			counter = 0;
			if (run && (config.backupEnabled || command)) {
				plugin.lock.lock();
				try {
					performBackup();
				} catch (Exception e) {
					e.printStackTrace();
				}
				plugin.lock.unlock();
			}

		}
		MessageLogger.debug("Graceful quit of AutoBackupThread");

	}


	public void performBackup() {

		command = false;

		if (config.backupsaveBefore) {
			plugin.saveThread.performSave();
		}

		long timestart = System.currentTimeMillis();

		MessageLogger.broadcast(configmsg.messageBackupBroadcastPre, config.backupBroadcast);

		if (config.backupLFSEnabled) {
			MessageLogger.debug("Starting LocalFS backup");
			new LocalFSBackup(config).performBackup();
			MessageLogger.debug("LocalFS backup finished");
		}

		if (config.backupFTPEnabled) {
			if (config.backupFTPSFTP) {
				MessageLogger.debug("Starting SFTP backup");
				new SFTPBackup(config).performBackup();
				MessageLogger.debug("SFTP backup finished");
			} else {
				MessageLogger.debug("Starting FTP backup");
				new FTPBackup(config).performBackup();
				MessageLogger.debug("FTP backup finished");
			}
		}

		if (config.backupScriptEnabled) {
			MessageLogger.debug("Starting Script backup");
			new ScriptBackup(config).performBackup();
			MessageLogger.debug("Script Backup Finished");
		}

		if (config.backupDropboxEnabled) {
			MessageLogger.debug("Starting Dropbox backup");
			new DropboxBackup(config).performBackup();
			MessageLogger.debug("Dropbox backup Finished");
		}

		MessageLogger.debug("Full backup time: "+(System.currentTimeMillis()-timestart)+" milliseconds");

		MessageLogger.broadcast(configmsg.messageBackupBroadcastPost, config.backupBroadcast);

	}

}




