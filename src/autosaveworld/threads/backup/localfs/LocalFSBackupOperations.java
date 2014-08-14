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

package autosaveworld.threads.backup.localfs;

import java.io.File;
import java.util.List;

import org.bukkit.World;

import autosaveworld.core.GlobalConstants;
import autosaveworld.core.logging.MessageLogger;
import autosaveworld.threads.backup.BackupUtils;
import autosaveworld.threads.backup.utils.ZipUtils;
import autosaveworld.utils.FileUtils;

public class LocalFSBackupOperations {

	private boolean zip;
	private String extpath;
	private List<String> excludefolders;

	public LocalFSBackupOperations(boolean zip, String extpath, List<String> excludefolders) {
		this.zip = zip;
		this.extpath = extpath;
		this.excludefolders = excludefolders;
	}

	public void backupWorld(World world, int maxBackupsCount, String latestbackuptimestamp, boolean disableWorldSaving) {
		MessageLogger.debug("Backuping world " + world.getWorldFolder().getName());

		boolean savestatus = world.isAutoSave();
		if (disableWorldSaving) {
			world.setAutoSave(false);
		}
		try {
			File fromfolder = world.getWorldFolder().getAbsoluteFile();
			String destfolder = extpath + File.separator + "backups" + File.separator + "worlds" + File.separator + world.getWorldFolder().getName();
			backupFolder(fromfolder, destfolder, maxBackupsCount, latestbackuptimestamp);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			world.setAutoSave(savestatus);
		}

		MessageLogger.debug("Backuped world " + world.getWorldFolder().getName());
	}

	public void backupPlugins(int maxBackupsCount, String latestbackuptimestamp) {
		try {
			File fromfolder = new File(GlobalConstants.getPluginsFolder()).getAbsoluteFile();
			String destfolder = extpath + File.separator + "backups" + File.separator + "plugins";
			backupFolder(fromfolder, destfolder, maxBackupsCount, latestbackuptimestamp);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void backupOtherFolders(List<String> folders, int maxBackupsCount, String latestbackuptimestamp) {
		for (String folder : folders) {
			MessageLogger.debug("Backuping folder " + folder);
			try {
				File fromfolder = new File(folder).getAbsoluteFile();
				String destfolder = extpath + File.separator + "backups" + File.separator + "others" + File.separator + fromfolder.getName();
				backupFolder(fromfolder, destfolder, maxBackupsCount, latestbackuptimestamp);
			} catch (Exception e) {
				e.printStackTrace();
			}
			MessageLogger.debug("Backuped folder " + folder);
		}
	}

	private void backupFolder(File fromfolder, String destfolder, int maxBackupsCount, String latestbackuptimestamp) {
		// check oldest backup count
		if ((maxBackupsCount != 0) && new File(destfolder).exists() && (new File(destfolder).list().length >= maxBackupsCount)) {
			// find oldest backup
			String oldestBackupName = BackupUtils.findOldestBackupName(new File(destfolder).list());
			// delete oldest backup
			if (oldestBackupName != null) {
				File oldestBakup = new File(destfolder, oldestBackupName);
				FileUtils.deleteDirectory(oldestBakup);
			}
		}
		String bfolder = destfolder + File.separator + latestbackuptimestamp;
		if (!zip) {
			LocalFSUtils.copyDirectory(fromfolder, new File(bfolder), excludefolders);
		} else {
			ZipUtils.zipFolder(fromfolder, new File(bfolder + ".zip"), excludefolders);
		}
	}

}
