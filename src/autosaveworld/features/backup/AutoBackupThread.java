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

package autosaveworld.features.backup;

import autosaveworld.config.AutoSaveWorldConfig;
import autosaveworld.config.AutoSaveWorldConfigMSG;
import autosaveworld.core.AutoSaveWorld;
import autosaveworld.core.logging.MessageLogger;
import autosaveworld.features.IntervalTaskThread;
import autosaveworld.features.backup.dropbox.DropboxBackup;
import autosaveworld.features.backup.ftp.FTPBackup;
import autosaveworld.features.backup.localfs.LocalFSBackup;
import autosaveworld.features.backup.script.ScriptBackup;
import autosaveworld.features.backup.sftp.SFTPBackup;

public class AutoBackupThread extends IntervalTaskThread {

	private AutoSaveWorld plugin;
	private AutoSaveWorldConfig config;
	private AutoSaveWorldConfigMSG configmsg;

	public AutoBackupThread(AutoSaveWorld plugin, AutoSaveWorldConfig config, AutoSaveWorldConfigMSG configmsg) {
		super("AutoBackupThread");
		this.plugin = plugin;
		this.config = config;
		this.configmsg = configmsg;
	}

	public static volatile boolean backupRunning = false;

	@Override
	public boolean isEnabled() {
		return config.backupEnabled;
	}

	@Override
	public int getInterval() {
		return config.backupInterval;
	}

	@Override
	public void doTask() throws Exception {
		backupRunning = true;
		performBackup();
		backupRunning = false;
	}

	public void performBackup() throws Exception {

		if (config.backupsaveBefore) {
			plugin.saveThread.performSave();
		}

		long timestart = System.currentTimeMillis();

		MessageLogger.broadcast(configmsg.messageBackupBroadcastPre, config.backupBroadcast);

		InputStreamConstruct.setRateLimit(config.backupRateLimit);

		if (config.backupLFSEnabled) {
			MessageLogger.debug("Starting LocalFS backup");
			try {
				new LocalFSBackup(config).performBackup();
				MessageLogger.debug("LocalFS backup finished");
			} catch (Exception e) {
				MessageLogger.exception("Error occured while performing LocalFS backup", e);
			}
		}

		if (config.backupFTPEnabled) {
			MessageLogger.debug("Starting FTP backup");
			try {
				new FTPBackup(config).performBackup();
				MessageLogger.debug("FTP backup finished");
			} catch (Exception e) {
				MessageLogger.exception("Error occured while performing FTP backup", e);
			}
		}

		if (config.backupSFTPEnabled) {
			MessageLogger.debug("Starting SFTP backup");
			try {
				new SFTPBackup(config).performBackup();
				MessageLogger.debug("SFTP backup finished");
			} catch (Exception e) {
				MessageLogger.exception("Error occured while performing SFTP backup", e);
			}
		}

		if (config.backupScriptEnabled) {
			MessageLogger.debug("Starting Script backup");
			try {
				new ScriptBackup(config).performBackup();
				MessageLogger.debug("Script Backup Finished");
			} catch (Exception e) {
				MessageLogger.exception("Error occured while performing Script backup", e);
			}
		}

		if (config.backupDropboxEnabled) {
			MessageLogger.debug("Starting Dropbox backup");
			try {
				new DropboxBackup(config).performBackup();
				MessageLogger.debug("Dropbox backup Finished");
			} catch (Exception e) {
				MessageLogger.exception("Error occured while performing DropBox backup", e);
			}
		}

		MessageLogger.debug("Full backup time: " + (System.currentTimeMillis() - timestart) + " milliseconds");

		MessageLogger.broadcast(configmsg.messageBackupBroadcastPost, config.backupBroadcast);

	}

}
