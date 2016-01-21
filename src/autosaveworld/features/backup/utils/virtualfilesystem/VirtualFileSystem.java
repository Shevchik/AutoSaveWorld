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
import java.util.Iterator;
import java.util.List;

public abstract class VirtualFileSystem {

	public void createAndChangeDirectory(String dirname) throws IOException {
		createDirectory(dirname);
		changeDirectory(dirname);
	}

	public abstract void changeDirectory(String dirname) throws IOException;

	public abstract void createDirectory(String dirname) throws IOException;

	public abstract void changeToParentDirectiry() throws IOException;

	public void deleteDirectoryRecursive(String dirname) throws IOException {
		if (isDirectory(dirname)) {
			changeDirectory(dirname);
			for (String file : getFiles()) {
				deleteDirectoryRecursive(file);
			}
			changeToParentDirectiry();
			deleteDirectory(dirname);
		} else {
			deleteFile(dirname);
		}
	}

	public abstract boolean isDirectory(String dirname) throws IOException;

	public abstract void deleteDirectory(String dirname) throws IOException;

	public abstract void deleteFile(String name) throws IOException;

	public List<String> getFiles() throws IOException {
		List<String> files = getFiles0();
		Iterator<String> iterator = files.iterator();
		while (iterator.hasNext()) {
			String name = iterator.next();
			if (name.equals(".") || name.equals("..")) {
				iterator.remove();
			}
		}
		return files;
	}

	protected abstract List<String> getFiles0() throws IOException;

	public abstract void createFile(String name, InputStream inputsteam) throws IOException;

}
