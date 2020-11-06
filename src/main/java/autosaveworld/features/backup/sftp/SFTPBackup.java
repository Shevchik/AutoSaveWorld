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
import autosaveworld.core.AutoSaveWorld;
import autosaveworld.features.backup.Backup;
import autosaveworld.features.backup.utils.virtualfilesystem.VirtualBackupManager;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class SFTPBackup extends Backup {

	public SFTPBackup() {
		super("SFTP");
	}

	public void performBackup() throws JSchException, IOException {
		AutoSaveWorldConfig config = AutoSaveWorld.getInstance().getMainConfig();

		JSch jsch = new JSch();
		Session session = jsch.getSession(config.backupSFTPUsername, config.backupSFTPHostname, config.backupSFTPPort);
		session.setTimeout(10000);
		session.setPassword(config.backupSFTPPassworld);
		session.connect();
		Channel channel = session.openChannel("sftp");
		channel.connect();
		ChannelSftp channelSftp = (ChannelSftp) channel;

		VirtualBackupManager.builder()
		.setBackupPath(config.backupSFTPPath)
		.setWorldList(config.backupSFTPWorldsList)
		.setBackupPlugins(config.backupSFTPPluginsFolder)
		.setOtherFolders(config.backupSFTPOtherFolders)
		.setExcludedFolders(config.backupSFTPExcludeFolders)
		.setMaxBackupNumber(config.backupSFTPMaxNumberOfBackups)
		.setZip(config.backupSFTPZipEnabled)
		.setVFS(new SFTPVirtualFileSystem(channelSftp))
		.create().backup();

		channelSftp.disconnect();
		session.disconnect();
	}

}
