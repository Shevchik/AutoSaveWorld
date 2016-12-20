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

package autosaveworld.features.backup.sftp;

import java.io.IOException;

import autosaveworld.config.AutoSaveWorldConfig;
import autosaveworld.features.backup.utils.virtualfilesystem.VirtualBackupManager;
import autosaveworld.zlibs.com.jcraft.jsch.Channel;
import autosaveworld.zlibs.com.jcraft.jsch.Channel.ChannelType;
import autosaveworld.zlibs.com.jcraft.jsch.ChannelSftp;
import autosaveworld.zlibs.com.jcraft.jsch.JSch;
import autosaveworld.zlibs.com.jcraft.jsch.JSchException;
import autosaveworld.zlibs.com.jcraft.jsch.Session;

public class SFTPBackup {

	private AutoSaveWorldConfig config;

	public SFTPBackup(AutoSaveWorldConfig config) {
		this.config = config;
	}

	public void performBackup() throws JSchException, IOException {
		JSch jsch = new JSch();
		Session session = jsch.getSession(config.backupFTPUsername, config.backupFTPHostname, config.backupFTPPort);
		session.setTimeout(10000);
		session.setPassword(config.backupFTPPassworld);
		session.connect();
		Channel channel = session.openChannel(ChannelType.SFTP);
		channel.connect();
		ChannelSftp channelSftp = (ChannelSftp) channel;

		VirtualBackupManager.builder()
		.setBackupPath(config.backupFTPPath)
		.setWorldList(config.backupFTPWorldsList)
		.setBackupPlugins(config.backupFTPPluginsFolder)
		.setOtherFolders(config.backupFTPOtherFolders)
		.setExcludedFolders(config.backupFTPExcludeFolders)
		.setMaxBackupNumber(config.backupFTPMaxNumberOfBackups)
		.setZip(config.backupFTPZipEnabled)
		.setVFS(new SFTPVirtualFileSystem(channelSftp))
		.create().backup();

		channelSftp.exit();
		session.disconnect();
	}

}
