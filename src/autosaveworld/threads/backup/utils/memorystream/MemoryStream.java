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

package autosaveworld.threads.backup.utils.memorystream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MemoryStream {

	private MemoryOutputStream os = new MemoryOutputStream();

	public MemoryOutputStream getOutputStream() {
		return os;
	}

	private MemoryInputStream is = new MemoryInputStream();

	public MemoryInputStream getInputStream() {
		return is;
	}

	public void putStreamEndSignal() {
		stream.putEndOfStreamSignal();
	}

	private MemoryStreamQueue stream = new MemoryStreamQueue(10 * 1024 * 1024);

	public class MemoryInputStream extends InputStream {

		private MemoryInputStream() {
		}

		@Override
		public int read() {
			return stream.take();
		}

		@Override
		public int read(byte[] b) {
			return read(b, 0, b.length);
		}

		@Override
		public int read(byte[] b, int off, int len) {
			return stream.take(b, off, len);
		}

	}

	public class MemoryOutputStream extends OutputStream {

		private MemoryOutputStream() {
		}

		@Override
		public void write(int b) {
			stream.put(b);
		}

		@Override
		public void write(byte b[]) throws IOException {
			write(b, 0, b.length);
		}

		@Override
		public void write(byte b[], int off, int len) {
			stream.put(b, off, len);
		}

	}

}
