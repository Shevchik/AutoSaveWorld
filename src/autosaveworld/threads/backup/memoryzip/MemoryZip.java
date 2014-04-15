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

package autosaveworld.threads.backup.memoryzip;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import autosaveworld.threads.backup.ExcludeManager;

public class MemoryZip {

	private MemoryZipOutputStream os = new MemoryZipOutputStream(this);
	private MemoryZipInputStream is = new MemoryZipInputStream(this);
	private ExecutorService executor = Executors.newSingleThreadExecutor();

	private final short END_OF_STREAM_SIGNAL = 1337;
	
	private LinkedBlockingQueue<Short> byteqeue = new LinkedBlockingQueue<Short>(10 * 1024 * 1024);
	protected int read() {
		try {
			int b = byteqeue.take();
			if (b == END_OF_STREAM_SIGNAL) {
				return -1;
			}
			if (b < 0) {
				b += 256;
			}
			return b;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return -1;
	}

	protected void write(byte b) {
		try {
			byteqeue.put((short) b);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static MemoryZipInputStream startZIP(final File inputDir, final List<String> excludefolders) {
		final MemoryZip mz = new MemoryZip();
		mz.executor.submit(
			new Runnable() {
				@Override
				public void run() {
					try {
						mz.zipFolder(inputDir, excludefolders);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		);
		mz.executor.shutdown();
		return mz.is;
	}

	private void zipFolder(final File srcDir, List<String> excludefolders) throws FileNotFoundException, IOException {
		try (BufferedOutputStream bos = new BufferedOutputStream(os)) {
			try (ZipOutputStream zipOutStream = new ZipOutputStream(bos)) {
				zipDir(excludefolders, zipOutStream, srcDir, "");
			}
		}
		try {
			byteqeue.put(END_OF_STREAM_SIGNAL);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void zipDir(List<String> excludefolders, ZipOutputStream zipOutStream, final File srcDir, String currentDir) throws IOException {
		final File zipDir = new File(srcDir, currentDir);

		for (final String child : zipDir.list()) {
			final File srcFile = new File(zipDir, child);

			if (srcFile.isDirectory()) {
				if (!ExcludeManager.isFolderExcluded(excludefolders, srcDir.getName() + File.separator + currentDir + child)) {
					zipDir(excludefolders, zipOutStream, srcDir, currentDir + child + File.separator);
				}
			} else {
				zipFile(zipOutStream, srcFile, srcDir.getName() + File.separator + currentDir + child);
			}
		}
	}

	private void zipFile(ZipOutputStream zipOutStream, final File srcFile, final String entry) throws IOException {
		if (!srcFile.getName().endsWith(".lck")) {
			try (InputStream inStream = new FileInputStream(srcFile)) {
				final ZipEntry zipEntry = new ZipEntry(entry);
				zipOutStream.putNextEntry(zipEntry);
				try {
					int len;
					final byte[] buf = new byte[4096];
					while ((len = inStream.read(buf)) > 0) {
						zipOutStream.write(buf, 0, len);
					}
				} finally {
					zipOutStream.closeEntry();
				}
			}
			Thread.yield();
		}
	}
	
}
