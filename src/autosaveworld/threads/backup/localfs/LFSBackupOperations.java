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
import java.util.concurrent.ExecutorService;

import org.bukkit.World;

import autosaveworld.core.AutoSaveWorld;
import autosaveworld.core.logging.MessageLogger;
import autosaveworld.threads.backup.utils.BackupFileUtils;
import autosaveworld.threads.backup.utils.ZipUtils;
import autosaveworld.utils.FileUtils;

public class LFSBackupOperations {

	private AutoSaveWorld plugin;
	private final boolean zip;
	private final String extpath;
	private final List<String> excludefolders;
	public LFSBackupOperations(AutoSaveWorld plugin, boolean zip, String extpath, List<String> excludefolders) {
		this.plugin = plugin;
		this.zip = zip;
		this.extpath = extpath;
		this.excludefolders = excludefolders;
	}

	public void startWorldBackup(ExecutorService backupService, final World world, final int maxBackupsCount, final String latestbackuptimestamp) {
		//create runnable
		Runnable backupWorld = new Runnable() {
			@Override
			public void run() {
				MessageLogger.debug("Starting backup for world "+world.getWorldFolder().getName());
				final String worldbackupfolder = extpath+File.separator+"backups"+File.separator+"worlds"+File.separator+world.getWorldFolder().getName();
				//check oldest backup count
				if (maxBackupsCount != 0 && new File(worldbackupfolder).exists() && new File(worldbackupfolder).list().length >= maxBackupsCount) {
					//remove oldest backup
					MessageLogger.debug("Deleting oldest backup for world "+world.getWorldFolder().getName());
					//find oldest backup
					String oldestBackupName = BackupFileUtils.findOldestBackupNameLFS(worldbackupfolder);
					//delete oldest backup
					if (oldestBackupName != null) {
						File oldestBakup = new File(worldbackupfolder + File.separator + oldestBackupName);
						FileUtils.deleteDirectory(oldestBakup);
					}
				}
				MessageLogger.debug("Backuping world "+world.getWorldFolder().getName());
				boolean savestatus = world.isAutoSave();
				world.setAutoSave(false);
				File worldfolder = world.getWorldFolder().getAbsoluteFile();
				String worldBackup = worldbackupfolder+File.separator+latestbackuptimestamp;
				if (!zip) {
					BackupFileUtils.copyDirectory(worldfolder, new File(worldBackup),excludefolders);
				} else {
					ZipUtils.zipFolder(worldfolder, new File(worldBackup+".zip"), excludefolders);
				}
				MessageLogger.debug("Backuped world "+world.getWorldFolder().getName());
				world.setAutoSave(savestatus);
			}
		};
		//add to executor
		backupService.submit(backupWorld);
	}


	public void startPluginsBackup(ExecutorService backupService,  final int maxBackupsCount, final String latestbackuptimestamp) {
		//create runnable
		Runnable backupPlugins = new Runnable() {
			@Override
			public void run() {
				MessageLogger.debug("Starting plugins backup");
				final String pluginsbackupfolder = extpath+File.separator+"backups"+File.separator+"plugins";
				//check oldest backup count
				if (maxBackupsCount != 0 && new File(pluginsbackupfolder).exists() && new File(pluginsbackupfolder).list().length >= maxBackupsCount) {
					//remove oldest backup
					MessageLogger.debug("Deleting oldest plugins backup");
					//find oldest backup
					String oldestBackupName = BackupFileUtils.findOldestBackupNameLFS(pluginsbackupfolder);
					//delete oldest backup
					if (oldestBackupName != null) {
						File oldestBakup = new File(pluginsbackupfolder + File.separator + oldestBackupName);
						FileUtils.deleteDirectory(oldestBakup);
					}
				}
				MessageLogger.debug("Backuping plugins");
				File pluginsfolder = plugin.getDataFolder().getParentFile().getAbsoluteFile();
				String pluginsBackup = extpath+File.separator+"backups"+File.separator+"plugins"+File.separator+latestbackuptimestamp;
				if (!zip) {
					BackupFileUtils.copyDirectory(pluginsfolder,new File(pluginsBackup), excludefolders);
				} else  {
					ZipUtils.zipFolder(pluginsfolder, new File(pluginsBackup+".zip"), excludefolders);
				}
				MessageLogger.debug("Backuped plugins");
			}
		};
		//add to executor
		backupService.submit(backupPlugins);
	}

	public void deleteOldestPluginsBackup(String oldestbackupdate) {
		String fldtodel = extpath+File.separator+"backups"+File.separator+"plugins"+File.separator+oldestbackupdate;
		FileUtils.deleteDirectory(new File(fldtodel));
		FileUtils.deleteDirectory(new File(fldtodel+".zip"));
	}

}
