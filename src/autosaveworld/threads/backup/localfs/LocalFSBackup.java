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

import java.text.SimpleDateFormat;

import org.bukkit.Bukkit;
import org.bukkit.World;

import autosaveworld.config.AutoSaveWorldConfig;

public class LocalFSBackup {

	private AutoSaveWorldConfig config;
	public LocalFSBackup(AutoSaveWorldConfig config) {
		this.config = config;
	}

	public void performBackup() {
		for (String extpath : config.backupLFSExtFolders) {

			//init backup operations class
			LFSBackupOperations bo = new LFSBackupOperations(config.backupLFSZipEnabled, extpath, config.backupLFSExcludeFolders);

			//create timestamp
			String backuptimestamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(System.currentTimeMillis());

			//backup worlds
			for (World world : Bukkit.getWorlds()) {
				//check if we need to backup this world
				if ((config.backupLFSBackupWorldsList).contains("*") || config.backupLFSBackupWorldsList.contains(world.getName())) {
					//backup world
					bo.backupWorld(world, config.backupLFSMaxNumberOfWorldsBackups, backuptimestamp);
				}
			}

			//backups plugins
			if (config.backupLFSPluginsFolder) {
				bo.backupPlugins(config.backupLFSMaxNumberOfPluginsBackups, backuptimestamp );
			}

		}
	}


}
