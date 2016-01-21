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

package autosaveworld.features.backup.sftp;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import autosaveworld.features.backup.utils.virtualfilesystem.VirtualFileSystem;
import autosaveworld.zlibs.com.jcraft.jsch.ChannelSftp;
import autosaveworld.zlibs.com.jcraft.jsch.SftpException;
import autosaveworld.zlibs.com.jcraft.jsch.ChannelSftp.LsEntry;

public class SFTPVirtualFileSystem extends VirtualFileSystem {

	private ChannelSftp sftpclient;
	public SFTPVirtualFileSystem(ChannelSftp sftpclient) {
		this.sftpclient = sftpclient;
	}

	@Override
	public void changeDirectory(String dirname) throws IOException {
		try {
			sftpclient.cd(dirname);
		} catch (SftpException ex) {
			throw wrapException(ex);
		}
	}

	@Override
	public void createDirectory(String dirname) throws IOException {
		try {
			if (getFiles().contains(dirname)) {
				return;
			}
			sftpclient.mkdir(dirname);
		} catch (SftpException ex) {
			throw wrapException(ex);
		}
	}

	@Override
	public void changeToParentDirectiry() throws IOException {
		try {
			sftpclient.cd("..");
		} catch (SftpException ex) {
			throw wrapException(ex);
		}
	}

	@Override
	public boolean isDirectory(String dirname) throws IOException {
		try {
			return sftpclient.stat(dirname).isDir();
		} catch (SftpException ex) {
			throw wrapException(ex);
		}
	}

	@Override
	public void deleteDirectory(String dirname) throws IOException {
		try {
			sftpclient.rmdir(dirname);
		} catch (SftpException ex) {
			throw wrapException(ex);
		}
	}

	@Override
	public void deleteFile(String name) throws IOException {
		try {
			sftpclient.rm(name);
		} catch (SftpException ex) {
			throw wrapException(ex);
		}
	}

	@Override
	protected List<String> getFiles0() throws IOException {
		try {
			ArrayList<String> files = new ArrayList<String>();
			Vector<LsEntry> names = sftpclient.ls(".");
			for (int i = 0; i < names.size(); i++) {
				files.add(names.get(i).getFilename());
			}
			return files;
		} catch (SftpException ex) {
			throw wrapException(ex);
		}
	}

	@Override
	public void createFile(String name, InputStream inputsteam) throws IOException {
		try {
			sftpclient.put(inputsteam, name);
		} catch (SftpException ex) {
			throw wrapException(ex);
		}
	}

	static IOException wrapException(SftpException ex) {
		return new IOException("sftp error", ex);
	}

}
