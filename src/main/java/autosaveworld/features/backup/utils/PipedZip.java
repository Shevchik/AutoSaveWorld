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

package autosaveworld.features.backup.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.List;

import autosaveworld.core.logging.MessageLogger;

public class PipedZip {

	public static InputStream startZIP(final File inputDir, final List<String> excludefolders) {
		PipedInputStream pipedin = new PipedInputStream(10 * 1024 * 1024);
		final PipedOutputStream pipedout = new PipedOutputStream();
		try {
			pipedout.connect(pipedin);
		} catch (IOException e) {
			MessageLogger.exception("Exception while connecting stream pipes", e);
		}
		final IOExceptionRethrowInputStream in = new IOExceptionRethrowInputStream(pipedin);
		new Thread("PipedZip files copy thread") {
			@Override
			public void run() {
				try {
					ZipUtils.zipFolder(inputDir, pipedout, excludefolders);
				} catch (IOException e) {
					in.exception(e);
				}
				try {
					pipedout.close();
				} catch (IOException e) {
					MessageLogger.exception("Exception while closing stream pipes", e);
				}
			}
		}.start();
		return in;
	}

	private static class IOExceptionRethrowInputStream extends InputStream {

		private final InputStream real;
		public IOExceptionRethrowInputStream(InputStream real) {
			this.real = real;
		}

		private volatile IOException exception;

		public void exception(IOException e) {
			this.exception = e;
		}

		@Override
		public int read() throws IOException {
			int res = real.read();
			if (exception != null) {
				throw exception;
			}
			return res;
		}

		public int read(byte b[], int off, int len) throws IOException {
			int res = real.read(b, off, len);
			if (exception != null) {
				throw exception;
			}
			return res;
		}

	}

}
