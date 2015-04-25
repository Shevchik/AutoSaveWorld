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

import autosaveworld.config.AutoSaveWorldConfig;
import autosaveworld.config.AutoSaveWorldConfigMSG;
import autosaveworld.core.AutoSaveWorld;
import autosaveworld.core.logging.MessageLogger;
import autosaveworld.threads.IntervalTaskThread;
import autosaveworld.threads.backup.dropbox.DropboxBackup;
import autosaveworld.threads.backup.ftp.FTPBackup;
import autosaveworld.threads.backup.localfs.LocalFSBackup;
import autosaveworld.threads.backup.script.ScriptBackup;
import autosaveworld.threads.backup.sftp.SFTPBackup;

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
	public void doTask() {
		backupRunning = true;
		try {
			performBackup();
		} catch (Throwable t) {
			t.printStackTrace();
		}
		backupRunning = false;
	}

	public void performBackup() {

		if (config.backupsaveBefore) {
			plugin.saveThread.performSave();
		}

		long timestart = System.currentTimeMillis();

		MessageLogger.broadcast(configmsg.messageBackupBroadcastPre, config.backupBroadcast);

		InputStreamConstruct.setRateLimit(config.backupRateLimit);

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

		MessageLogger.debug("Full backup time: " + (System.currentTimeMillis() - timestart) + " milliseconds");

		MessageLogger.broadcast(configmsg.messageBackupBroadcastPost, config.backupBroadcast);

	}

}
