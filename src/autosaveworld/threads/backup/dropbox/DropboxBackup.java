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

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.World;

import autosaveworld.config.AutoSaveWorldConfig;
import autosaveworld.core.logging.MessageLogger;
import autosaveworld.threads.backup.BackupUtils;
import autosaveworld.zlibs.com.dropbox.core.DbxClient;
import autosaveworld.zlibs.com.dropbox.core.DbxEntry;
import autosaveworld.zlibs.com.dropbox.core.DbxRequestConfig;

public class DropboxBackup {

	private AutoSaveWorldConfig config;
	public DropboxBackup(AutoSaveWorldConfig config) {
		this.config = config;
	}

	private DbxRequestConfig dconfig = new DbxRequestConfig("AutoSaveWorld", Locale.getDefault().toString());

	public void performBackup() {
		try {
			//init
			DbxClient client = new DbxClient(dconfig, config.backupDropboxAPPTOKEN);
			//create dirs
			client.createFolder("/"+config.backupDropboxPath+"/backups");			
			//delete oldest backup
			List<DbxEntry> entries = client.getMetadataWithChildren("/"+config.backupDropboxPath+"/backups").children;
			String[] listnames = new String[entries.size()];
			for (int i = 0; i < entries.size(); i++) {
				listnames[i] = entries.get(i).name;
			}
			if (config.backupDropboxMaxNumberOfBackups != 0 && listnames.length >= config.backupDropboxMaxNumberOfBackups) {
				MessageLogger.debug("Deleting oldest backup");
				//find oldest backup
				String oldestBackup = BackupUtils.findOldestBackupName(listnames);
				//delete oldest backup
				if (oldestBackup != null) {
					DropboxUtils.deleteDirectory(client, "/"+config.backupDropboxPath+"/backups/"+oldestBackup);
				}
			}
			//create a dir for new backup
			String datedir = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(System.currentTimeMillis());
			client.createFolder("/"+config.backupDropboxPath+"/backups/"+datedir);
			//load BackupOperations class
			DropboxBackupOperations bo = new DropboxBackupOperations(client, "/"+config.backupDropboxPath+"/backups/"+datedir, config.backupDropboxZipEnabled, config.backupDropboxExcludeFolders);
			//do worlds backup
			if (!config.backupFTPBackupWorldsList.isEmpty()) {
				MessageLogger.debug("Backuping Worlds");
				for (World w : Bukkit.getWorlds()) {
					if (config.backupFTPBackupWorldsList.contains("*") || config.backupFTPBackupWorldsList.contains(w.getWorldFolder().getName())) {
						bo.backupWorld(w);
					}
				}
				MessageLogger.debug("Backuped Worlds");
			}
			//do plugins backup
			if (config.backupFTPPluginsFolder) {
				MessageLogger.debug("Backuping plugins");
				bo.backupPlugins();
				MessageLogger.debug("Backuped plugins");
			}
			//backup other folders
			if (!config.backupFTPOtherFolders.isEmpty()) {
				MessageLogger.debug("Backuping other folders");
				bo.backupOtherFolders(config.backupFTPOtherFolders);
				MessageLogger.debug("Backuped other folders");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
