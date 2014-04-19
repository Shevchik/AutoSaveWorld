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

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.bukkit.World;

import autosaveworld.core.AutoSaveWorld;
import autosaveworld.core.logging.MessageLogger;
import autosaveworld.libs.org.apache.commons.net.ftp.FTPClient;
import autosaveworld.threads.backup.utils.BackupFileUtils;

public class FTPBackupOperations {

	private AutoSaveWorld plugin;
	final boolean zip;
	final List<String> excludefolders;
	private FTPClient ftp;
	public FTPBackupOperations(AutoSaveWorld plugin, FTPClient ftp, boolean zip,  List<String> excludefolders) {
		this.plugin = plugin;
		this.zip = zip;
		this.excludefolders = excludefolders;
		this.ftp = ftp;
	}

	public void backupWorld(World world) {
		MessageLogger.debug("Backuping world "+world.getWorldFolder().getName());
		boolean savestaus = world.isAutoSave();
		world.setAutoSave(false);
		try {
			File worldfolder = world.getWorldFolder().getCanonicalFile();
			if (!zip) {
				BackupFileUtils.uploadDirectoryToFTP(ftp, worldfolder, excludefolders);
			} else {
				BackupFileUtils.zipAndUploadDirectoryToFTP(ftp, worldfolder, excludefolders);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			world.setAutoSave(savestaus);
		}

		MessageLogger.debug("Backuped world "+world.getWorldFolder().getName());
	}


	public void backupPlugins() {
		try {
			File plfolder = plugin.getDataFolder().getParentFile().getCanonicalFile();
			if (!zip) {
				BackupFileUtils.uploadDirectoryToFTP(ftp, plfolder, excludefolders);
			} else  {
				BackupFileUtils.zipAndUploadDirectoryToFTP(ftp, plfolder, excludefolders);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
