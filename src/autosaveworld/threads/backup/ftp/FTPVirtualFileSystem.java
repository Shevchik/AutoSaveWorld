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

package autosaveworld.threads.backup.ftp;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import autosaveworld.threads.backup.utils.virtualfilesystem.VirtualFileSystem;
import autosaveworld.zlibs.org.apache.commons.net.ftp.FTPClient;

public class FTPVirtualFileSystem extends VirtualFileSystem {

	private FTPClient ftpclient;
	public FTPVirtualFileSystem(FTPClient ftpclient) {
		this.ftpclient = ftpclient;
	}

	@Override
	public void changeDirectory(String dirname) throws IOException {
		ftpclient.changeWorkingDirectory(dirname);
	}

	@Override
	public void createDirectory(String dirname) throws IOException {
		ftpclient.makeDirectory(dirname);
	}

	@Override
	public void changeToParentDirectiry() throws IOException {
		ftpclient.changeToParentDirectory();
	}

	@Override
	public boolean isDirectory(String dirname) throws IOException {
		boolean isDirectory = ftpclient.changeWorkingDirectory(dirname);
		if (isDirectory) {
			ftpclient.changeToParentDirectory();
		}
		return isDirectory;
	}

	@Override
	public void deleteDirectory(String dirname) throws IOException {
		ftpclient.removeDirectory(dirname);
	}

	@Override
	public void deleteFile(String name) throws IOException {
		ftpclient.deleteFile(name);
	}

	@Override
	protected List<String> getFiles0() throws IOException {
		return new ArrayList<String>(Arrays.asList(ftpclient.listNames()));
	}

	@Override
	public void createFile(String name, InputStream inputsteam) throws IOException {
		ftpclient.storeFile(name, inputsteam);
	}

}
