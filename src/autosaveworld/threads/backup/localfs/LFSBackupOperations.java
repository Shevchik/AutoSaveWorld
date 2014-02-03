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
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.bukkit.World;

import autosaveworld.core.AutoSaveWorld;
import autosaveworld.threads.backup.Zip;

public class LFSBackupOperations {

	AutoSaveWorld plugin;
	final boolean zip;
	final String extpath;
	final List<String> excludefolders;
	public LFSBackupOperations(AutoSaveWorld plugin, boolean zip, String extpath, List<String> excludefolders)
	{
		this.plugin = plugin;
		this.zip = zip;
		this.extpath = extpath;
		this.excludefolders = excludefolders;
	}

	private LFSFileUtils fu = new LFSFileUtils();

	public void startWorldBackup(ExecutorService backupService, final World world, final int maxBackupsCount, final String latestbackuptimestamp)
	{
		//create runnable
		Runnable backupWorld = new Runnable()
		{
			@Override
			public void run()
			{
				plugin.debug("Starting backup for world "+world.getWorldFolder().getName());
				final String worldbackupfolder = extpath+File.separator+"backups"+File.separator+"worlds"+File.separator+world.getWorldFolder().getName();
				//check oldest backup count
				if (maxBackupsCount != 0 && new File(worldbackupfolder).exists() && new File(worldbackupfolder).list().length >= maxBackupsCount)
				{
					//remove oldest backup
					plugin.debug("Deleting oldest backup for world "+world.getWorldFolder().getName());
					//find oldest backup
					String oldestBackupName = fu.findOldestBackupName(worldbackupfolder);
					//delete oldest backup
					File oldestBakup = new File(worldbackupfolder + File.separator + oldestBackupName);
					if (oldestBackupName.contains(".zip")) {
						oldestBakup.delete();
					} else {
						fu.deleteDirectory(oldestBakup);
					}
				}
				plugin.debug("Backuping world "+world.getWorldFolder().getName());
				try {
					world.setAutoSave(false);
					File worldfolder = world.getWorldFolder().getCanonicalFile();
					String worldBackup = worldbackupfolder+File.separator+latestbackuptimestamp;
					if (!zip) {
						fu.copyDirectory(worldfolder, new File(worldBackup),excludefolders);
					} else {
						Zip zipfld = new Zip(excludefolders);
						zipfld.ZipFolder(worldfolder, new File(worldBackup+".zip"));
					}
					plugin.debug("Backuped world "+world.getWorldFolder().getName());
				} catch (Exception e) {
					plugin.debug("Failed to backup world "+world.getWorldFolder().getName());
					e.printStackTrace();
				} finally {
					world.setAutoSave(true);
				}
			}
		};
		//add to executor
		backupService.submit(backupWorld);
	}


	public void startPluginsBackup(ExecutorService backupService,  final int maxBackupsCount, final String latestbackuptimestamp)
	{
		//create runnable
		Runnable backupPlugins = new Runnable()
		{
			@Override
			public void run()
			{
				plugin.debug("Starting plugins backup");
				final String pluginsbackupfolder = extpath+File.separator+"backups"+File.separator+"plugins";
				//check oldest backup count
				if (maxBackupsCount != 0 && new File(pluginsbackupfolder).exists() && new File(pluginsbackupfolder).list().length >= maxBackupsCount)
				{
					//remove oldest backup
					plugin.debug("Deleting oldest plugins backup");
					//find oldest backup
					String oldestBackupName = fu.findOldestBackupName(pluginsbackupfolder);
					//delete oldest backup
					File oldestBakup = new File(pluginsbackupfolder + File.separator + oldestBackupName);
					if (oldestBackupName.contains(".zip")) {
						oldestBakup.delete();
					} else {
						fu.deleteDirectory(oldestBakup);
					}
				}
				plugin.debug("Backuping plugins");
				try {
					File pluginsfolder = plugin.getDataFolder().getParentFile().getCanonicalFile();
					String pluginsBackup = extpath+File.separator+"backups"+File.separator+"plugins"+File.separator+latestbackuptimestamp;
					if (!zip) {
						fu.copyDirectory(pluginsfolder,new File(pluginsBackup),excludefolders);
					} else  {
						Zip zipfld = new Zip(excludefolders);
						zipfld.ZipFolder(pluginsfolder,new File(pluginsBackup+".zip"));
					}
					plugin.debug("Backuped plugins");
				} catch (IOException e) {
					plugin.debug("Failed to backup plugins");
					e.printStackTrace();
				}
			}
		};
		//add to executor
		backupService.submit(backupPlugins);
	}

	public void deleteOldestPluginsBackup(String oldestbackupdate)
	{
		String fldtodel = extpath+File.separator+"backups"+File.separator+"plugins"+File.separator+oldestbackupdate;
		fu.deleteDirectory(new File(fldtodel));
		fu.deleteDirectory(new File(fldtodel+".zip"));
	}

}
