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

import java.text.MessageFormat;
import java.util.ArrayList;

import autosaveworld.config.AutoSaveWorldConfig;
import autosaveworld.core.AutoSaveWorld;
import autosaveworld.core.logging.MessageLogger;
import autosaveworld.features.backup.dropbox.DropboxBackup;
import autosaveworld.features.backup.ftp.FTPBackup;
import autosaveworld.features.backup.googledrive.GoogleDriveBackup;
import autosaveworld.features.backup.localfs.LocalFSBackup;
import autosaveworld.features.backup.script.ScriptBackup;
import autosaveworld.features.backup.sftp.SFTPBackup;
import autosaveworld.utils.Threads.IntervalTaskThread;

public class AutoBackupThread extends IntervalTaskThread {

	public AutoBackupThread() {
		super("AutoBackupThread");
	}

	private boolean backupRunning = false;

	public boolean isBackupInProcess() {
		return backupRunning;
	}

	@Override
	public boolean isEnabled() {
		return AutoSaveWorld.getInstance().getMainConfig().backupEnabled;
	}

	@Override
	public int getInterval() {
		return AutoSaveWorld.getInstance().getMainConfig().backupInterval;
	}

	@Override
	public void doTask() throws Exception {
		backupRunning = true;
		try {
			performBackup();
		} finally {
			backupRunning = false;
		}
	}

	public void performBackup() throws Exception {
		AutoSaveWorldConfig config = AutoSaveWorld.getInstance().getMainConfig();

		if (config.backupsaveBefore) {
			AutoSaveWorld.getInstance().getSaveThread().performSave();
		}

		long timestart = System.currentTimeMillis();

		MessageLogger.broadcast(AutoSaveWorld.getInstance().getMessageConfig().messageBackupBroadcastPre, config.backupBroadcast);

		InputStreamFactory.setRateLimit(config.backupRateLimit);

		ArrayList<Backup> backups = new ArrayList<Backup>();

		if (config.backupLFSEnabled) {
			backups.add(new LocalFSBackup());
		}
		if (config.backupFTPEnabled) {
			backups.add(new FTPBackup());
		}
		if (config.backupSFTPEnabled) {
			backups.add(new SFTPBackup());
		}
		if (config.backupScriptEnabled) {
			backups.add(new ScriptBackup());
		}
		if (config.backupDropboxEnabled) {
			backups.add(new DropboxBackup());
		}
		if (config.backupGDriveEnabled) {
			backups.add(new GoogleDriveBackup());
		}

		for (Backup backup : backups) {
			MessageLogger.debug(MessageFormat.format("Starting {0} backup", backup.getName()));
			try {
				backup.performBackup();
				MessageLogger.debug(MessageFormat.format("Finished {0} backup", backup.getName()));
			} catch (Throwable t) {
				MessageLogger.exception(MessageFormat.format("Failed {0} backup", backup.getName()), t);
			}
		}

		MessageLogger.debug(MessageFormat.format("Backup took {0} milliseconds", System.currentTimeMillis() - timestart));

		MessageLogger.broadcast(AutoSaveWorld.getInstance().getMessageConfig().messageBackupBroadcastPost, config.backupBroadcast);

	}

}
