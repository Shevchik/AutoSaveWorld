/**
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 */

package autosaveworld.features.backup.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import autosaveworld.core.logging.MessageLogger;
import autosaveworld.features.backup.ExcludeManager;
import autosaveworld.features.backup.InputStreamConstruct;
import autosaveworld.utils.FileUtils;

public class ZipUtils {

	public static void zipFolder(final File srcDir, final File destFile, List<String> excludefolders) {
		destFile.getParentFile().mkdirs();

		try (OutputStream fos = new FileOutputStream(destFile)) {
			zipFolder(srcDir, fos, excludefolders);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void zipFolder(final File srcDir, final OutputStream outputStream, List<String> excludefolders) {
		try (BufferedOutputStream bufOutStream = new BufferedOutputStream(outputStream)) {
			try (ZipOutputStream zipOutStream = new ZipOutputStream(bufOutStream)) {
				zipDir(excludefolders, zipOutStream, srcDir, "");
			}
			bufOutStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void zipDir(List<String> excludefolders, ZipOutputStream zipOutStream, final File srcDir, String currentDir) throws IOException {
		final File zipDir = new File(srcDir, currentDir);

		for (String child : FileUtils.safeList(zipDir)) {
			File srcFile = new File(zipDir, child);

			if (srcFile.isDirectory()) {
				if (!ExcludeManager.isFolderExcluded(excludefolders, srcDir.getName() + File.separator + currentDir + child)) {
					zipDir(excludefolders, zipOutStream, srcDir, currentDir + child + File.separator);
				}
			} else {
				zipFile(zipOutStream, srcFile, srcDir.getName() + File.separator + currentDir + child);
			}
		}
	}

	private static void zipFile(ZipOutputStream zipOutStream, final File srcFile, final String entry) throws IOException {
		//any io exception happening after putting zip entry kills the archive, so we do some checks before actually writing it
		//check can read
		if (!srcFile.canRead()) {
			MessageLogger.warn("Failed to backup file: "+srcFile.getAbsolutePath() + ", reason: canRead() returned false");
			return;
		}
		InputStream inStream = null;
		try {
			//first attempt to construct the input stream, may throw exception if file gone missing or some other thing happened
			inStream = InputStreamConstruct.getFileInputStream(srcFile);
		} catch (IOException e) {
			MessageLogger.warn("Failed to backup file: "+srcFile.getAbsolutePath() + ", reason: exception when opening reading channel: "+e.getMessage());
			return;
		}
		if (inStream != null) {
			int firstByte = -1;
			//check if we can read from input stream
			try {
				firstByte = inStream.read();
			} catch (IOException e) {
				MessageLogger.warn("Failed to backup file: "+srcFile.getAbsolutePath() + ", reason: exception when reading first byte: "+e.getMessage());
				return;
			}
			//empty file, put entry anyway
			if (firstByte == -1) {
				ZipEntry zipEntry = new ZipEntry(entry);
				zipEntry.setTime(srcFile.lastModified());
				zipOutStream.putNextEntry(zipEntry);
				zipOutStream.closeEntry();
				return;
			}
			//finally copy file
			ZipEntry zipEntry = new ZipEntry(entry);
			zipEntry.setTime(srcFile.lastModified());
			zipOutStream.putNextEntry(zipEntry);
			zipOutStream.write(firstByte);
			final byte[] buf = new byte[8192];

			int len;
			while ((len = inStream.read(buf)) != -1) {
				zipOutStream.write(buf, 0, len);
			}

			zipOutStream.closeEntry();

			try {
				inStream.close();
			} catch (IOException e) {
			}
		}
	}

}
