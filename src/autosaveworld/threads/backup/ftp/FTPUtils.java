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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import autosaveworld.threads.backup.ExcludeManager;
import autosaveworld.threads.backup.utils.MemoryZip;
import autosaveworld.zlibs.org.apache.commons.net.ftp.FTPClient;

public class FTPUtils {

	public static void uploadDirectory(FTPClient ftp, File src, List<String> excludefolders) throws IOException {
		if (src.isDirectory()) {
			ftp.makeDirectory(src.getName());
			ftp.changeWorkingDirectory(src.getName());
			for (File file : src.listFiles()) {
				if (!ExcludeManager.isFolderExcluded(excludefolders, file.getPath())) {
					uploadDirectory(ftp, file, excludefolders);
				}
			}
			ftp.changeToParentDirectory();
		} else {
			if (!src.getName().endsWith(".lck")) {
				try {
					InputStream is = new FileInputStream(src);
					storeFile(ftp, is, src.getName());
					is.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void zipAndUploadDirectory(FTPClient ftp, File src, List<String> excludefolders) throws IOException {
		InputStream is = MemoryZip.startZIP(src, excludefolders);
		storeFile(ftp, is, src.getName() + ".zip");
		is.close();
	}

	private static void storeFile(FTPClient ftp, InputStream is, String filename) {
		try {
			ftp.storeFile(filename, is);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			Thread.sleep(0);
		} catch (InterruptedException e) {
		}
	}

	public static void deleteDirectory(FTPClient ftp, String directory) throws IOException {
		if (ftp.changeWorkingDirectory(directory)) {
			String[] files = ftp.listNames();
			if (files != null) {
				for (String name : files) {
					deleteDirectory(ftp, name);
				}
			}
			ftp.changeToParentDirectory();
			ftp.removeDirectory(directory);
		} else {
			ftp.deleteFile(directory);
		}
	}

}
