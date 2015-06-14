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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

public class BackupUtils {

	public static String findOldestBackupName(String[] backups) {
		return findOldestBackupName(Arrays.asList(backups));
	}

	public static String findOldestBackupName(List<String> backups) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
		String oldestBackupName = null;
		long old = System.currentTimeMillis();
		for (String timestampString : backups) {
			try {
				long cur;
				if (timestampString.endsWith(".zip")) {
					cur = sdf.parse(timestampString.substring(0, timestampString.indexOf(".zip"))).getTime();
				} else {
					cur = sdf.parse(timestampString).getTime();
				}
				if (cur < old) {
					old = cur;
					oldestBackupName = timestampString;
				}
			} catch (ParseException e) {
			}
		}
		return oldestBackupName;
	}

}
