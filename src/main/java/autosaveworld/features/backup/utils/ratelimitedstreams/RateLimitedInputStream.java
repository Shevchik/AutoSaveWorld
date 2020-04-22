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

package autosaveworld.features.backup.utils.ratelimitedstreams;

import java.io.IOException;
import java.io.InputStream;

public class RateLimitedInputStream extends InputStream {

	private InputStream inputstream;
	private long bytesperms;

	public RateLimitedInputStream(InputStream inputstream, long kbps) {
		this.inputstream = inputstream;
		bytesperms = (kbps * 1024) / 1000;
	}

	private long startTime = 0;
	private long bytesRead = 0;

	private void sleepIfNeeded(long bytesToRead) {
		if (startTime == 0) {
			startTime = System.currentTimeMillis();
			bytesRead += bytesToRead;
			return;
		}
		long readTime = System.currentTimeMillis() - startTime;
		if ((bytesRead + bytesToRead) > (readTime * bytesperms)) {
			try {
				Thread.sleep(((bytesRead + bytesToRead) / bytesperms) - readTime);
			} catch (InterruptedException e) {
			}
		}
		bytesRead += bytesToRead;
	}

	@Override
	public int read() throws IOException {
		sleepIfNeeded(1);
		return inputstream.read();
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		sleepIfNeeded(len);
		return inputstream.read(b, off, len);
	}

	@Override
	public void close() throws IOException {
		inputstream.close();
	}

}
