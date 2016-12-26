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

package autosaveworld.features.backup.dropbox;

import java.io.IOException;

import autosaveworld.config.AutoSaveWorldConfig;
import autosaveworld.core.AutoSaveWorld;
import autosaveworld.features.backup.Backup;
import autosaveworld.features.backup.utils.virtualfilesystem.VirtualBackupManager;
import autosaveworld.zlibs.com.dropbox.core.DbxRequestConfig;
import autosaveworld.zlibs.com.dropbox.core.v2.DbxClientV2;

public class DropboxBackup extends Backup {

	public DropboxBackup() {
		super("Dropbox");
	}

	private final DbxRequestConfig dconfig = DbxRequestConfig.newBuilder(AutoSaveWorld.getInstance().getName()).withAutoRetryEnabled().withUserLocaleFromPreferences().build();

	public void performBackup() throws IOException {
		AutoSaveWorldConfig config = AutoSaveWorld.getInstance().getMainConfig();

		DbxClientV2 client = new DbxClientV2(dconfig, config.backupDropboxAPPTOKEN);

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
	}

}
