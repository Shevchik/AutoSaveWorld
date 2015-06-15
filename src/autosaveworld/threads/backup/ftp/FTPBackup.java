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

import autosaveworld.config.AutoSaveWorldConfig;
import autosaveworld.core.logging.MessageLogger;
import autosaveworld.threads.backup.utils.virtualfilesystem.VirtualBackupManager;
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
			FTPClient ftpclient = new FTPClient();

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

			ftpclient.setFileType(FTP.BINARY_FILE_TYPE);

			VirtualBackupManager.builder()
			.setBackupPath(config.backupFTPPath)
			.setWorldList(config.backupFTPWorldsList)
			.setBackupPlugins(config.backupFTPPluginsFolder)
			.setOtherFolders(config.backupFTPOtherFolders)
			.setExcludedFolders(config.backupFTPExcludeFolders)
			.setMaxBackupNumber(config.backupFTPMaxNumberOfBackups)
			.setZip(config.backupFTPZipEnabled)
			.setVFS(new FTPVirtualFileSystem(ftpclient))
			.create().backup();

			ftpclient.logout();
		} catch (Throwable e) {
			MessageLogger.warn("Error occured while performing a backup");
			e.printStackTrace();
		}
	}

}
