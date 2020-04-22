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

package autosaveworld.features.backup.localfs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

import autosaveworld.core.logging.MessageLogger;
import autosaveworld.features.backup.BackupUtils;
import autosaveworld.features.backup.InputStreamFactory;
import autosaveworld.utils.FileUtils;

public class LocalFSUtils {

	public static void copyDirectory(File sourceLocation, File targetLocation, List<String> excludefolders) {
		if (sourceLocation.isDirectory()) {
			targetLocation.mkdirs();
			for (String filename : FileUtils.safeList(sourceLocation)) {
				if (!BackupUtils.isFolderExcluded(excludefolders, new File(sourceLocation, filename).getPath())) {
					copyDirectory(new File(sourceLocation, filename), new File(targetLocation, filename), excludefolders);
				}
			}
		} else {
			if (!InputStreamFactory.isRateLimited()) {
				try {
					Files.copy(sourceLocation.toPath(), targetLocation.toPath(), StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					MessageLogger.warn("Failed to backup file: " + sourceLocation);
					targetLocation.delete();
				}
			} else {
				try (InputStream is = InputStreamFactory.getFileInputStream(sourceLocation)) {
					Files.copy(is, targetLocation.toPath(), StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					MessageLogger.warn("Failed to backup file: " + sourceLocation);
					targetLocation.delete();
				}
			}
		}
	}

}
