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

package autosaveworld.threads.backup;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import autosaveworld.threads.backup.utils.ratelimitedstreams.RateLimitedInputStream;

public class InputStreamConstruct {

	private static long rate = 0;

	protected static void setRateLimit(long rate) {
		InputStreamConstruct.rate = rate;
	}

	public static boolean isRateLimited() {
		return rate > 0;
	}

	public static InputStream getFileInputStream(File file) throws FileNotFoundException {
		InputStream stream = new RandomAccessFileInputStream(new RandomAccessFile(file, "r"));
		if (rate > 0) {
			stream = new RateLimitedInputStream(stream, rate);
		}
		return stream;
	}

	public static class RandomAccessFileInputStream extends InputStream {

		private RandomAccessFile raf;
		public RandomAccessFileInputStream(RandomAccessFile raf) {
			this.raf = raf;
		}

		@Override
		public int read() throws IOException {
			return raf.read();
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			return raf.read(b, off, len);
		}

		@Override
		public void close() throws IOException {
			raf.close();
		}

	}

}
