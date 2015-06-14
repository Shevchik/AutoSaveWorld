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

package autosaveworld.threads.backup.dropbox;

import java.util.Locale;

import autosaveworld.config.AutoSaveWorldConfig;
import autosaveworld.threads.backup.utils.virtualfilesystem.VirtualBackupManager;
import autosaveworld.zlibs.com.dropbox.core.DbxClient;
import autosaveworld.zlibs.com.dropbox.core.DbxRequestConfig;

public class DropboxBackup {

	private AutoSaveWorldConfig config;

	public DropboxBackup(AutoSaveWorldConfig config) {
		this.config = config;
	}

	private DbxRequestConfig dconfig = new DbxRequestConfig("AutoSaveWorld", Locale.getDefault().toString());

	public void performBackup() {
		try {
			DbxClient client = new DbxClient(dconfig, config.backupDropboxAPPTOKEN);

			VirtualBackupManager.builder()
			.setBackupPath(config.backupDropboxPath)
			.setWorldList(config.backupDropboxWorldsList)
			.setBackupPlugins(config.backupDropboxPluginsFolder)
			.setOtherFolders(config.backupDropboxOtherFolders)
			.setExcludedFolders(config.backupDropboxExcludeFolders)
			.setMaxBackupNumber(config.backupDropboxMaxNumberOfBackups)
			.setZip(config.backupDropboxZipEnabled)
			.setVFS(new DropboxVirtualFileSystem(client))
			.create().backup();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
