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

	public static volatile boolean backupRunning = false;

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
		performBackup();
		backupRunning = false;
	}

	public void performBackup() throws Exception {

		if (AutoSaveWorld.getInstance().getMainConfig().backupsaveBefore) {
			AutoSaveWorld.getInstance().getSaveThread().performSave();
		}

		long timestart = System.currentTimeMillis();

		MessageLogger.broadcast(AutoSaveWorld.getInstance().getMessageConfig().messageBackupBroadcastPre, AutoSaveWorld.getInstance().getMainConfig().backupBroadcast);

		InputStreamConstruct.setRateLimit(AutoSaveWorld.getInstance().getMainConfig().backupRateLimit);

		if (AutoSaveWorld.getInstance().getMainConfig().backupLFSEnabled) {
			MessageLogger.debug("Starting LocalFS backup");
			try {
				new LocalFSBackup().performBackup();
				MessageLogger.debug("LocalFS backup finished");
			} catch (Exception e) {
				MessageLogger.exception("Error occured while performing LocalFS backup", e);
			}
		}

		if (AutoSaveWorld.getInstance().getMainConfig().backupFTPEnabled) {
			MessageLogger.debug("Starting FTP backup");
			try {
				new FTPBackup().performBackup();
				MessageLogger.debug("FTP backup finished");
			} catch (Exception e) {
				MessageLogger.exception("Error occured while performing FTP backup", e);
			}
		}

		if (AutoSaveWorld.getInstance().getMainConfig().backupSFTPEnabled) {
			MessageLogger.debug("Starting SFTP backup");
			try {
				new SFTPBackup().performBackup();
				MessageLogger.debug("SFTP backup finished");
			} catch (Exception e) {
				MessageLogger.exception("Error occured while performing SFTP backup", e);
			}
		}

		if (AutoSaveWorld.getInstance().getMainConfig().backupScriptEnabled) {
			MessageLogger.debug("Starting Script backup");
			try {
				new ScriptBackup().performBackup();
				MessageLogger.debug("Script Backup Finished");
			} catch (Exception e) {
				MessageLogger.exception("Error occured while performing Script backup", e);
			}
		}

		if (AutoSaveWorld.getInstance().getMainConfig().backupDropboxEnabled) {
			MessageLogger.debug("Starting Dropbox backup");
			try {
				new DropboxBackup().performBackup();
				MessageLogger.debug("Dropbox backup Finished");
			} catch (Exception e) {
				MessageLogger.exception("Error occured while performing DropBox backup", e);
			}
		}

		if (AutoSaveWorld.getInstance().getMainConfig().backupGDriveEnabled) {
			MessageLogger.debug("Starting Google Drive backup");
			try {
				new GoogleDriveBackup().performBackup();
				MessageLogger.debug("Google Drive backup finished");
			} catch (Exception e) {
				MessageLogger.exception("Error occured while performing Google Drive backup", e);
			}
		}

		MessageLogger.debug("Full backup time: " + (System.currentTimeMillis() - timestart) + " milliseconds");

		MessageLogger.broadcast(AutoSaveWorld.getInstance().getMessageConfig().messageBackupBroadcastPost, AutoSaveWorld.getInstance().getMainConfig().backupBroadcast);

	}

}
