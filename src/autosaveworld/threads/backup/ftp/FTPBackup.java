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

import java.text.SimpleDateFormat;

import org.bukkit.Bukkit;
import org.bukkit.World;

import autosaveworld.config.AutoSaveConfig;
import autosaveworld.core.AutoSaveWorld;
import autosaveworld.threads.backup.BackupFileUtils;
import autosaveworldsrclibs.org.apache.commons.net.ftp.FTP;
import autosaveworldsrclibs.org.apache.commons.net.ftp.FTPClient;
import autosaveworldsrclibs.org.apache.commons.net.ftp.FTPReply;

public class FTPBackup {


	private AutoSaveWorld plugin;
	private AutoSaveConfig config;
	public FTPBackup(AutoSaveWorld plugin, AutoSaveConfig config)
	{
		this.plugin = plugin;
		this.config = config;
	}





	public void performBackup()
	{
		FTPClient ftpclient = new FTPClient();
		try {
			//connect
			ftpclient.connect(config.ftphostname, config.ftpport);
			if (!FTPReply.isPositiveCompletion(ftpclient.getReplyCode())) {
				ftpclient.disconnect();
				Bukkit.getLogger().severe("[AutoSaveWorld] Failed to connect to ftp server. Backup to ftp server failed");
			}
			ftpclient.login(config.ftpusername, config.ftppassworld);
			//create dirs
			ftpclient.makeDirectory(config.ftppath);
			ftpclient.changeWorkingDirectory(config.ftppath);
			ftpclient.makeDirectory("backups");
			ftpclient.changeWorkingDirectory("backups");
			//delete oldest backup
			String[] listnames = ftpclient.listNames();
			if (config.ftpbackupmaxnumberofbackups != 0 && listnames != null && listnames.length >= config.ftpbackupmaxnumberofbackups)
			{
				plugin.debug("Deleting oldest backup");
				//find oldest backup
				String oldestBackup = BackupFileUtils.findOldestBackupNameFTP(listnames);
				//delete oldest backup
				BackupFileUtils.deleteDirectoryFromFTP(ftpclient, oldestBackup);
			}
			String datedir = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(System.currentTimeMillis());
			ftpclient.makeDirectory(datedir);
			ftpclient.changeWorkingDirectory(datedir);
			ftpclient.setFileType(FTP.BINARY_FILE_TYPE);
			//load BackupOperations class
			FTPBackupOperations bo = new FTPBackupOperations(plugin, ftpclient, config.ftpbackupzip, config.ftpbackupexcludefolders);
			//do worlds backup
			plugin.debug("Backuping Worlds");
			for (World w : Bukkit.getWorlds())
			{
				if (config.ftpbackupWorlds.contains("*") || config.ftpbackupWorlds.contains(w.getWorldFolder().getName()))
				{
					bo.backupWorld(w);
				}
			}
			plugin.debug("Backuped Worlds");
			//do plugins backup
			if (config.ftpbackuppluginsfolder)
			{
				plugin.debug("Backuping plugins");
				bo.backupPlugins();
				plugin.debug("Backuped plugins");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
