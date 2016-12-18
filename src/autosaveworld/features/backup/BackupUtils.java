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

package autosaveworld.features.backup;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import autosaveworld.utils.FileUtils;
import autosaveworld.utils.StringUtils;

public class BackupUtils {

	public static String findOldestBackupName(String[] backups) {
		return findOldestBackupName(Arrays.asList(backups));
	}

	public static String findOldestBackupName(Collection<String> backups) {
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

	public static boolean isFolderExcluded(List<String> excludelist, String folderPath) {
		String folderName = FileUtils.getAbsoluteFileName(folderPath);
	
		for (String excludedFolder : excludelist) {
			//asterisk at the end of excluded folder path excludes any folders starting with excluded folder pathname
			if (excludedFolder.endsWith("*")) {
				String excludedFolderName = FileUtils.getAbsoluteFileName(StringUtils.eraseRight(excludedFolder, 1));
				if (folderName.startsWith(excludedFolderName)) {
					return true;
				}
			} else {
				if (folderName.equals(FileUtils.getAbsoluteFileName(excludedFolder))) {
					return true;
				}
			}
		}

		return false;
	}

}
