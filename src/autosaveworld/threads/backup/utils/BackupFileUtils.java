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

package autosaveworld.threads.backup.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.List;

import autosaveworld.libs.org.apache.commons.net.ftp.FTPClient;
import autosaveworld.threads.backup.ExcludeManager;
import autosaveworld.threads.backup.utils.memoryzip.MemoryZip;

public class BackupFileUtils {

	public static void copyDirectory(File sourceLocation , File targetLocation, List<String> excludefolders) {
		if (sourceLocation.isDirectory()) {
			if (!targetLocation.exists()) {
				targetLocation.mkdirs();
			}
			for (String filename : sourceLocation.list()) {
				if (!ExcludeManager.isFolderExcluded(excludefolders, new File(sourceLocation, filename).getPath())) {
					copyDirectory(new File(sourceLocation, filename), new File(targetLocation, filename), excludefolders);
				}
			}
		} else {
			if (!sourceLocation.getName().endsWith(".lck")) {
				try {
					Files.copy(sourceLocation.toPath(), targetLocation.toPath());
				} catch (Exception e) {
					e.printStackTrace();
				}
				Thread.yield();
			}
		}
	}

	public static void deleteDirectory(File file) {
		if(!file.exists()) {
			return;
		}
		if(file.isDirectory()) {
			for(File f : file.listFiles()) {
				deleteDirectory(f);
			}
			file.delete();
		} else {
			file.delete();
		}
	}

	public static String findOldestBackupNameLFS(String backupsfodler) {
		String[] timestamps = new File(backupsfodler).list();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
		String oldestBackupName = null;
		long old = System.currentTimeMillis();
		for (String timestampString : timestamps) {
			try {
				long cur = System.currentTimeMillis();
				if (timestampString.endsWith(".zip")) {
					cur = sdf.parse(timestampString.substring(0,timestampString.indexOf(".zip"))).getTime();
				} else {
					cur = sdf.parse(timestampString).getTime();
				}
				if (cur < old) {
					old = cur;
					oldestBackupName = timestampString;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return oldestBackupName;
	}

	public static void uploadDirectoryToFTP(FTPClient ftp, File src, List<String> excludefolders) throws IOException {
		if (src.isDirectory()) {
			ftp.makeDirectory(src.getName());
			ftp.changeWorkingDirectory(src.getName());
			for (File file : src.listFiles()) {
				if (!ExcludeManager.isFolderExcluded(excludefolders, file.getPath())) {
					uploadDirectoryToFTP(ftp, file, excludefolders);
				}
			}
			ftp.changeToParentDirectory();
		} else {
			if (!src.getName().endsWith(".lck")) {
				try {
					InputStream is = new FileInputStream(src);
					ftp.storeFile(src.getName(), is);
					is.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
				Thread.yield();
			}
		}
	}

	public static void zipAndUploadDirectoryToFTP(FTPClient ftp, File src, List<String> excludefolders) {
		InputStream is = MemoryZip.startZIP(src, excludefolders);
		try {
			ftp.storeFile(src.getName()+".zip", is);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void deleteDirectoryFromFTP(FTPClient ftp, String directory) throws IOException {
		if (ftp.changeWorkingDirectory(directory)) {
			String[] files = ftp.listNames();
			if (files != null) {
				for (String name : files) {
					deleteDirectoryFromFTP(ftp, name);
				}
			}
			ftp.changeToParentDirectory();
			ftp.removeDirectory(directory);
		} else {
			ftp.deleteFile(directory);
		}
	}

	public static String findOldestBackupNameFTP(String[] timestamps) throws IOException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
		String oldestBackupName = timestamps[0];
		try {
			long old = sdf.parse(oldestBackupName).getTime();
			for (String timestampString : timestamps) {
				long cur = sdf.parse(timestampString).getTime();
				if (cur < old) {
					old = cur;
					oldestBackupName = timestampString;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return oldestBackupName;
	}

}
