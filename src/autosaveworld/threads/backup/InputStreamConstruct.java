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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import autosaveworld.threads.backup.utils.ratelimitedstreams.RateLimitedInputStream;

public class InputStreamConstruct {

	private static int rate = 0;

	protected static void setRateLimit(int rate) {
		InputStreamConstruct.rate = rate;
	}

	public static InputStream getFileInputStream(String path) throws FileNotFoundException {
		return getFileInputStream(new File(path));
	}

	public static InputStream getFileInputStream(File file) throws FileNotFoundException {
		InputStream stream = new FileInputStream(file);
		if (rate > 0) {
			stream = new RateLimitedInputStream(stream, rate);
		}
		return stream;
	}

}
