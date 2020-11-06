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

package autosaveworld.features.backup.localfs;

import java.text.SimpleDateFormat;

import org.bukkit.Bukkit;
import org.bukkit.World;

import autosaveworld.config.AutoSaveWorldConfig;
import autosaveworld.core.AutoSaveWorld;
import autosaveworld.core.logging.MessageLogger;
import autosaveworld.features.backup.Backup;

public class LocalFSBackup extends Backup {

	public LocalFSBackup() {
		super("Local FileSystem");
	}

	public void performBackup() {
		AutoSaveWorldConfig config = AutoSaveWorld.getInstance().getMainConfig();
		for (String extpath : config.backupLFSExtFolders) {
			LocalFSBackupOperations bo = new LocalFSBackupOperations(config.backupLFSZipEnabled, extpath, config.backupLFSExcludeFolders);
			String backuptimestamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(System.currentTimeMillis());
			for (World world : Bukkit.getWorlds()) {
				if ((config.backupLFSBackupWorldsList).contains("*") || config.backupLFSBackupWorldsList.contains(world.getName())) {
					MessageLogger.debug("Backuping Worlds");
					bo.backupWorld(world, config.backupLFSMaxNumberOfWorldsBackups, backuptimestamp);
					MessageLogger.debug("Backuped Worlds");
				}
			}
			if (config.backupLFSPluginsFolder) {
				MessageLogger.debug("Backuping plugins");
				bo.backupPlugins(config.backupLFSMaxNumberOfPluginsBackups, backuptimestamp);
				MessageLogger.debug("Backuped plugins");
			}
			if (!config.backupLFSOtherFolders.isEmpty()) {
				MessageLogger.debug("Backuping other folders");
				bo.backupOtherFolders(config.backupLFSOtherFolders, config.backupLFSMaxNumberOfOtherBackups, backuptimestamp);
				MessageLogger.debug("Backuped other folders");
			}
		}
	}

}
