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

package autosaveworld.features.backup.utils.virtualfilesystem;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import autosaveworld.utils.FileUtils;

public abstract class VirtualFileSystem {

	public void createAndEnterDirectory(String dirname) throws IOException {
		createDirectory(dirname);
		enterDirectory(dirname);
	}

	public void enterDirectory(String dirname) throws IOException {
		for (String path : FileUtils.splitPath(dirname)) {
			enterDirectory0(path);
		}
	}

	protected abstract void enterDirectory0(String dirname) throws IOException;

	public void createDirectory(String dirname) throws IOException {
		int createdCount = 0;
		for (String path : FileUtils.splitPath(dirname)) {
			if (!exists(path)) {
				createDirectory0(path);	
			}
			enterDirectory0(path);
			createdCount++;
		}
		while (createdCount-- > 0) {
			leaveDirectory();
		}
	}

	protected abstract void createDirectory0(String dirname) throws IOException;

	public abstract void leaveDirectory() throws IOException;

	public void deleteDirectoryRecursive(String dirname) throws IOException {
		if (isDirectory(dirname)) {
			enterDirectory(dirname);
			for (String file : getEntries()) {
				deleteDirectoryRecursive(file);
			}
			leaveDirectory();
			deleteDirectory(dirname);
		} else {
			deleteFile(dirname);
		}
	}

	public abstract boolean exists(String path) throws IOException;

	public abstract boolean isDirectory(String dirname) throws IOException;

	public abstract void deleteDirectory(String dirname) throws IOException;

	public abstract void deleteFile(String name) throws IOException;

	public abstract Set<String> getEntries() throws IOException;

	public abstract void createFile(String name, InputStream inputsteam) throws IOException;

}
