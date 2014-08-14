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

package autosaveworld.threads.backup.ftp;

import java.text.SimpleDateFormat;

import org.bukkit.Bukkit;
import org.bukkit.World;

import autosaveworld.config.AutoSaveWorldConfig;
import autosaveworld.core.logging.MessageLogger;
import autosaveworld.threads.backup.BackupUtils;
import autosaveworld.zlibs.org.apache.commons.net.ftp.FTP;
import autosaveworld.zlibs.org.apache.commons.net.ftp.FTPClient;
import autosaveworld.zlibs.org.apache.commons.net.ftp.FTPReply;

public class FTPBackup {

	private AutoSaveWorldConfig config;

	public FTPBackup(AutoSaveWorldConfig config) {
		this.config = config;
	}

	public void performBackup() {
		try {
			// init
			FTPClient ftpclient = new FTPClient();
			// connect
			ftpclient.connect(config.backupFTPHostname, config.backupFTPPort);
			if (!FTPReply.isPositiveCompletion(ftpclient.getReplyCode())) {
				ftpclient.disconnect();
				MessageLogger.warn("Failed to connect to ftp server. Backup to ftp server failed");
				return;
			}
			if (!ftpclient.login(config.backupFTPUsername, config.backupFTPPassworld)) {
				ftpclient.disconnect();
				MessageLogger.warn("Failed to connect to ftp server. Backup to ftp server failed");
				return;
			}
			// set file type
			ftpclient.setFileType(FTP.BINARY_FILE_TYPE);
			// create dirs
			ftpclient.makeDirectory(config.backupFTPPath);
			ftpclient.changeWorkingDirectory(config.backupFTPPath);
			ftpclient.makeDirectory("backups");
			ftpclient.changeWorkingDirectory("backups");
			// delete oldest backup
			String[] listnames = ftpclient.listNames();
			if ((config.backupFTPMaxNumberOfBackups != 0) && (listnames != null) && (listnames.length >= config.backupFTPMaxNumberOfBackups)) {
				MessageLogger.debug("Deleting oldest backup");
				// find oldest backup
				String oldestBackup = BackupUtils.findOldestBackupName(listnames);
				// delete oldest backup
				if (oldestBackup != null) {
					FTPUtils.deleteDirectory(ftpclient, oldestBackup);
				}
			}
			// create a dir for new backup
			String datedir = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(System.currentTimeMillis());
			ftpclient.makeDirectory(datedir);
			ftpclient.changeWorkingDirectory(datedir);
			// load BackupOperations class
			FTPBackupOperations bo = new FTPBackupOperations(ftpclient, config.backupFTPZipEnabled, config.backupFTPExcludeFolders);
			// do worlds backup
			if (!config.backupFTPBackupWorldsList.isEmpty()) {
				MessageLogger.debug("Backuping Worlds");
				ftpclient.makeDirectory("worlds");
				ftpclient.changeWorkingDirectory("worlds");
				for (World w : Bukkit.getWorlds()) {
					if (config.backupFTPBackupWorldsList.contains("*") || config.backupFTPBackupWorldsList.contains(w.getWorldFolder().getName())) {
						bo.backupWorld(w, config.backupDisableWorldSaving);
					}
				}
				ftpclient.changeToParentDirectory();
				MessageLogger.debug("Backuped Worlds");
			}
			// do plugins backup
			if (config.backupFTPPluginsFolder) {
				MessageLogger.debug("Backuping plugins");
				ftpclient.makeDirectory("plugins");
				ftpclient.changeWorkingDirectory("plugins");
				bo.backupPlugins();
				ftpclient.changeToParentDirectory();
				MessageLogger.debug("Backuped plugins");
			}
			// backup other folders
			if (!config.backupFTPOtherFolders.isEmpty()) {
				MessageLogger.debug("Backuping other folders");
				ftpclient.makeDirectory("others");
				ftpclient.changeWorkingDirectory("others");
				bo.backupOtherFolders(config.backupFTPOtherFolders);
				ftpclient.changeToParentDirectory();
				MessageLogger.debug("Backuped other folders");
			}
			// disconnect
			ftpclient.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
