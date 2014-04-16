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

package autosaveworld.threads.backup.utils.memoryzip;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import autosaveworld.threads.backup.utils.ZipUtils;

public class MemoryZip {

	private MemoryZipOutputStream os = new MemoryZipOutputStream(this);
	private MemoryZipInputStream is = new MemoryZipInputStream(this);
	private ExecutorService executor = Executors.newSingleThreadExecutor();

	private PrimitiveIntLinkedBlockingQueue bytequeue = new PrimitiveIntLinkedBlockingQueue(10 * 1024 * 1024);
	protected int read() {
		return bytequeue.take();
	}

	protected void write(int b) {
		bytequeue.put(b);
	}

	public static MemoryZipInputStream startZIP(final File inputDir, final List<String> excludefolders) {
		final MemoryZip mz = new MemoryZip();
		mz.executor.submit(
			new Runnable() {
				@Override
				public void run() {
					ZipUtils.zipFolder(inputDir, mz.os, excludefolders);
					mz.bytequeue.put(-1);
				}
			}
		);
		mz.executor.shutdown();
		return mz.is;
	}

}
