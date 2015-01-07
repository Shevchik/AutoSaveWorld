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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Vector;

import autosaveworld.threads.backup.ExcludeManager;
import autosaveworld.threads.backup.InputStreamConstruct;
import autosaveworld.threads.backup.utils.MemoryZip;
import autosaveworld.zlibs.com.jcraft.jsch.ChannelSftp;
import autosaveworld.zlibs.com.jcraft.jsch.ChannelSftp.LsEntry;
import autosaveworld.zlibs.com.jcraft.jsch.SftpException;

public class SFTPUtils {

	public static void uploadDirectory(ChannelSftp sftp, File src, List<String> excludefolders) throws SftpException {
		if (src.isDirectory()) {
			sftp.mkdir(src.getName());
			sftp.cd(src.getName());
			for (File file : src.listFiles()) {
				if (!ExcludeManager.isFolderExcluded(excludefolders, file.getPath())) {
					uploadDirectory(sftp, file, excludefolders);
				}
			}
			sftp.cd("..");
		} else {
			if (!src.getName().endsWith(".lck")) {
				try (InputStream is = InputStreamConstruct.getFileInputStream(src)) {
					storeFile(sftp, is, src.getName() + ".zip");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void zipAndUploadDirectory(ChannelSftp sftp, File src, List<String> excludefolders) throws IOException {
		InputStream is = MemoryZip.startZIP(src, excludefolders);
		storeFile(sftp, is, src.getName() + ".zip");
		is.close();
	}

	private static void storeFile(ChannelSftp sftp, InputStream is, String filename) {
		try {
			sftp.put(is, filename);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void deleteDirectory(ChannelSftp sftp, String oldestBackup) throws SftpException {
		if (isDir(sftp, oldestBackup)) {
			sftp.cd(oldestBackup);
			Vector<LsEntry> entries = sftp.ls(".");
			for (LsEntry entry : entries) {
				deleteDirectory(sftp, entry.getFilename());
			}
			sftp.cd("..");
			sftp.rmdir(oldestBackup);
		} else {
			sftp.rm(oldestBackup);
		}
	}

	private static boolean isDir(ChannelSftp sftp, String entry) throws SftpException {
		return sftp.stat(entry).isDir();
	}

	public static boolean dirExists(ChannelSftp sftp, String dir) throws SftpException {
		Vector<LsEntry> names = sftp.ls(".");
		for (LsEntry entry : names) {
			if (entry.getFilename().equals(dir)) {
				return true;
			}
		}
		return false;
	}

}
