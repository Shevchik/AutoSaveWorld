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

package autosaveworld.threads.backup;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import autosaveworld.config.AutoSaveConfig;

public class Zip {
	AutoSaveConfig config;

	Zip(AutoSaveConfig config) {
		this.config = config;
	}

	private ZipOutputStream zipOutStream;

	public void ZipFolder(final File srcDir, final File destFile)
			throws FileNotFoundException, IOException {
		destFile.getParentFile().mkdirs();
		final FileOutputStream outStream = new FileOutputStream(destFile);

		try {
			final BufferedOutputStream bufOutStream = new BufferedOutputStream(
					outStream, 4096);
			try {
				zipOutStream = new ZipOutputStream(bufOutStream);
				try {
					zipDir(srcDir, "");
				} finally {
					zipOutStream.close();
				}
			} finally {
				bufOutStream.close();
			}
		} finally {
			outStream.close();
		}
	}

	private void zipDir(final File srcDir, String currentDir)
			throws IOException {

		if (!"".equals(currentDir)) {
			currentDir += File.separator;
		}

		final File zipDir = new File(srcDir, currentDir);

		for (final String child : zipDir.list()) {
			final File srcFile = new File(zipDir, child);

			if (srcFile.isDirectory()) {
				boolean copy = true;
				for (String efname : config.excludefolders) {
					if ((new File(srcDir.getName() + File.separator
							+ currentDir + child).getAbsoluteFile())
							.equals(new File(efname).getAbsoluteFile())) {
						copy = false;
						break;
					}
				}
				if (copy) {
					zipDir(srcDir, currentDir + child);
				}
			} else
				zipFile(srcFile, srcDir.getName() + File.separator + currentDir
						+ child);
		}
	}

	private void zipFile(final File srcFile, final String entry)
			throws IOException {
		final InputStream inStream = new FileInputStream(srcFile);
		try {
			final ZipEntry zipEntry = new ZipEntry(entry);
			zipEntry.setTime(srcFile.lastModified());
			zipOutStream.putNextEntry(zipEntry);

			final byte[] buf = new byte[4096];
			int len;

			try {
				while ((len = inStream.read(buf)) > -1)
					if (len > 0)
						zipOutStream.write(buf, 0, len);
			} catch (final IOException e) {
			} finally {
				zipOutStream.closeEntry();
			}
		} finally {
			inStream.close();
		}
	}
}
