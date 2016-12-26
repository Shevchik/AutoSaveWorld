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

package autosaveworld.features.backup.ftp;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import autosaveworld.features.backup.utils.virtualfilesystem.VirtualFileSystem;
import autosaveworld.zlibs.org.apache.commons.net.ftp.FTPClient;

public class FTPVirtualFileSystem extends VirtualFileSystem {

	private final FTPClient ftpclient;
	public FTPVirtualFileSystem(FTPClient ftpclient) {
		this.ftpclient = ftpclient;
	}

	@Override
	public void enterDirectory0(String dirname) throws IOException {
		boolean result = ftpclient.changeWorkingDirectory(dirname);
		if (!result) {
			throw new IOException("Changing working directory failed");
		}
	}

	@Override
	public void createDirectory0(String dirname) throws IOException {
		boolean result = ftpclient.makeDirectory(dirname);
		if (!result) {
			throw new IOException("Creating directory failed");
		}
	}

	@Override
	public void leaveDirectory() throws IOException {
		boolean result = ftpclient.changeToParentDirectory();
		if (!result) {
			throw new IOException("Leaving working directory failed");
		}
	}

	@Override
	public boolean exists(String path) throws IOException {
		//there is no standard way to check if path exists
		//list files and use contains check, that may take a lot of time
		return getEntries().contains(path);
	}

	@Override
	public boolean isDirectory(String dirname) throws IOException {
		//there is no standard way to check if path is directory or get path metadata
		//so just try to enter the path and assume positive result as path being a directory
		boolean isDirectory = ftpclient.changeWorkingDirectory(dirname);
		if (isDirectory) {
			ftpclient.changeToParentDirectory();
		}
		return isDirectory;
	}

	@Override
	public void deleteDirectory(String dirname) throws IOException {
		boolean result = ftpclient.removeDirectory(dirname);
		if (!result) {
			throw new IOException("Deleting file failed");
		}
	}

	@Override
	public void deleteFile(String name) throws IOException {
		boolean result = ftpclient.deleteFile(name);
		if (!result) {
			throw new IOException("Deleting directory failed");
		}
	}

	@Override
	public Set<String> getEntries() throws IOException {
		return new HashSet<String>(Arrays.asList(ftpclient.listNames()));
	}

	@Override
	public void createFile(String name, InputStream inputsteam) throws IOException {
		ftpclient.storeFile(name, inputsteam);
	}

}
