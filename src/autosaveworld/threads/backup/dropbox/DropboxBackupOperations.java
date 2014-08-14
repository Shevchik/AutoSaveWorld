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

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.bukkit.World;

import autosaveworld.core.GlobalConstants;
import autosaveworld.core.logging.MessageLogger;
import autosaveworld.zlibs.com.dropbox.core.DbxClient;
import autosaveworld.zlibs.com.dropbox.core.DbxException;

public class DropboxBackupOperations {

	private DbxClient client;
	private String path;
	private List<String> excludefolders;
	private boolean zip;

	public DropboxBackupOperations(DbxClient client, String path, boolean zip, List<String> excludefolders) {
		this.client = client;
		this.path = path;
		this.zip = zip;
		this.excludefolders = excludefolders;
	}

	public void backupWorld(World world, boolean disableWorldSaving) {
		MessageLogger.debug("Backuping world " + world.getWorldFolder().getName());

		boolean savestatus = world.isAutoSave();
		if (disableWorldSaving) {
			world.setAutoSave(false);
		}
		try {
			File fromfolder = world.getWorldFolder().getAbsoluteFile();
			String destfolder = path + "/worlds/" + world.getWorldFolder().getName();
			backupFolder(fromfolder, destfolder);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			world.setAutoSave(savestatus);
		}

		MessageLogger.debug("Backuped world " + world.getWorldFolder().getName());
	}

	public void backupPlugins() {
		try {
			File fromfolder = new File(GlobalConstants.getPluginsFolder()).getAbsoluteFile();
			String destfolder = path + "/plugins";
			backupFolder(fromfolder, destfolder);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void backupOtherFolders(List<String> folders) {
		for (String folder : folders) {
			MessageLogger.debug("Backuping folder " + folder);
			try {
				File fromfolder = new File(folder).getAbsoluteFile();
				String destfolder = path + "/others/" + fromfolder.getName();
				backupFolder(fromfolder, destfolder);
			} catch (Exception e) {
				e.printStackTrace();
			}
			MessageLogger.debug("Backuped folder " + folder);
		}
	}

	private void backupFolder(File fromfolder, String destfolder) throws IOException, DbxException {
		if (!zip) {
			DropboxUtils.uploadDirectory(client, fromfolder, path, excludefolders);
		} else {
			DropboxUtils.zipAndUploadDirectory(client, fromfolder, path, excludefolders);
		}
	}

}
