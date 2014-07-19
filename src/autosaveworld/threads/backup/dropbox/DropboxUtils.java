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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import autosaveworld.threads.backup.ExcludeManager;
import autosaveworld.threads.backup.utils.MemoryZip;
import autosaveworld.zlibs.com.dropbox.core.DbxClient;
import autosaveworld.zlibs.com.dropbox.core.DbxException;
import autosaveworld.zlibs.com.dropbox.core.DbxStreamWriter;
import autosaveworld.zlibs.com.dropbox.core.DbxWriteMode;

public class DropboxUtils {

	public static void uploadDirectory(DbxClient client, File src, String path, List<String> excludefolders) throws IOException, DbxException {
		if (src.isDirectory()) {
			client.createFolder(path + "/" + src.getName());
			for (File file : src.listFiles()) {
				if (!ExcludeManager.isFolderExcluded(excludefolders, file.getPath())) {
					uploadDirectory(client, file, path + "/" + src.getName(), excludefolders);
				}
			}
		} else {
			if (!src.getName().endsWith(".lck")) {
				try {
					InputStream is = new FileInputStream(src);
					storeFile(client, is, path, src.getName());
					is.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void zipAndUploadDirectory(DbxClient dbx, File src, String path, List<String> excludefolders) throws IOException {
		InputStream is = MemoryZip.startZIP(src, excludefolders);
		storeFile(dbx, is, path, src.getName()+".zip");
		is.close();
	}

	private static void storeFile(DbxClient client, InputStream is, String path, String filename) {
		try {
			client.uploadFileChunked(path+"/"+filename, DbxWriteMode.force(), -1, new DbxStreamWriter.InputStreamCopier(is));
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {Thread.sleep(0);} catch (InterruptedException e) {}
	}

	public static void deleteDirectory(DbxClient client, String directory) throws DbxException {
		client.delete(directory);
	}

}
