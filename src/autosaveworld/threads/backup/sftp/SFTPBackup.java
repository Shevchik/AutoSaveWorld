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

package autosaveworld.threads.backup.sftp;

import java.text.SimpleDateFormat;
import java.util.Vector;

import org.bukkit.Bukkit;
import org.bukkit.World;

import autosaveworld.config.AutoSaveWorldConfig;
import autosaveworld.core.logging.MessageLogger;
import autosaveworld.threads.backup.BackupUtils;
import autosaveworld.zlibs.com.jcraft.jsch.Channel;
import autosaveworld.zlibs.com.jcraft.jsch.Channel.ChannelType;
import autosaveworld.zlibs.com.jcraft.jsch.ChannelSftp;
import autosaveworld.zlibs.com.jcraft.jsch.ChannelSftp.LsEntry;
import autosaveworld.zlibs.com.jcraft.jsch.JSch;
import autosaveworld.zlibs.com.jcraft.jsch.Session;

public class SFTPBackup {

	private AutoSaveWorldConfig config;
	public SFTPBackup(AutoSaveWorldConfig config) {
		this.config = config;
	}

	public void performBackup() {
		try {
			//init
			JSch jsch = new JSch();
			//connect
			Session session = jsch.getSession(config.backupFTPUsername, config.backupFTPHostname, config.backupFTPPort);
			session.setTimeout(10000);
			session.setPassword(config.backupFTPPassworld);
			session.connect();
			Channel channel = session.openChannel(ChannelType.SFTP);
			channel.connect();
			ChannelSftp channelSftp = (ChannelSftp) channel;
			//create dirs
			if (!SFTPUtils.dirExists(channelSftp, config.backupFTPPath)) {
				channelSftp.mkdir(config.backupFTPPath);
			}
			channelSftp.cd(config.backupFTPPath);
			if (!SFTPUtils.dirExists(channelSftp, "backups")) {
				channelSftp.mkdir("backups");
			}
			channelSftp.cd("backups");
			//delete oldest backup
			Vector<LsEntry> names = channelSftp.ls(".");
			String[] listnames = new String[names.size()];
			for (int i = 0; i < names.size(); i++) {
				listnames[i] = names.get(i).getFilename();
			}
			if (config.backupFTPMaxNumberOfBackups != 0 && listnames.length >= config.backupFTPMaxNumberOfBackups) {
				MessageLogger.debug("Deleting oldest backup");
				//find oldest backup
				String oldestBackup = BackupUtils.findOldestBackupName(listnames);
				//delete oldest backup
				SFTPUtils.deleteDirectory(channelSftp, oldestBackup);
			}
			//create a dir for new backup
			String datedir = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(System.currentTimeMillis());
			channelSftp.mkdir(datedir);
			channelSftp.cd(datedir);
			//load BackupOperations class
			SFTPBackupOperations bo = new SFTPBackupOperations(channelSftp, config.backupFTPZipEnabled, config.backupFTPExcludeFolders);
			//do worlds backup
			if (!config.backupFTPBackupWorldsList.isEmpty()) {
				MessageLogger.debug("Backuping Worlds");
				channelSftp.mkdir("worlds");
				channelSftp.cd("worlds");
				for (World w : Bukkit.getWorlds()) {
					if (config.backupFTPBackupWorldsList.contains("*") || config.backupFTPBackupWorldsList.contains(w.getWorldFolder().getName())) {
						bo.backupWorld(w, config.backupDisableWorldSaving);
					}
				}
				channelSftp.cd("..");
				MessageLogger.debug("Backuped Worlds");
			}
			//do plugins backup
			if (config.backupFTPPluginsFolder) {
				MessageLogger.debug("Backuping plugins");
				channelSftp.mkdir("plugins");
				channelSftp.cd("plugins");
				bo.backupPlugins();
				channelSftp.cd("..");
				MessageLogger.debug("Backuped plugins");
			}
			//backup other folders
			if (!config.backupFTPOtherFolders.isEmpty()) {
				MessageLogger.debug("Backuping other folders");
				channelSftp.mkdir("others");
				channelSftp.cd("others");
				bo.backupOtherFolders(config.backupFTPOtherFolders);
				channelSftp.cd("..");
				MessageLogger.debug("Backuped other folders");
			}
			//disconnect
			channelSftp.exit();
			session.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
