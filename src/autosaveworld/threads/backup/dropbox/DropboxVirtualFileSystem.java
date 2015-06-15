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

package autosaveworld.threads.backup.dropbox;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import autosaveworld.threads.backup.utils.virtualfilesystem.VirtualFileSystem;
import autosaveworld.zlibs.com.dropbox.core.DbxClient;
import autosaveworld.zlibs.com.dropbox.core.DbxEntry;
import autosaveworld.zlibs.com.dropbox.core.DbxException;
import autosaveworld.zlibs.com.dropbox.core.DbxStreamWriter;
import autosaveworld.zlibs.com.dropbox.core.DbxWriteMode;

public class DropboxVirtualFileSystem extends VirtualFileSystem {

	private DbxClient dbxclient;
	private String currentpath = "";
	public DropboxVirtualFileSystem(DbxClient dbxclient) {
		this.dbxclient = dbxclient;
	}

	@Override
	public void changeDirectory(String dirname) throws IOException {
		currentpath += "/" + dirname;
	}

	@Override
	public void createDirectory(String dirname) throws IOException {
		try {
			dbxclient.createFolder(getPath(dirname));
		} catch (DbxException e) {
			throw wrapException(e);
		}
	}

	@Override
	public void changeToParentDirectiry() throws IOException {
        if (currentpath.isEmpty()) {
        	throw new IOException("Can't get parent directory of a root dir");
        }
        currentpath = currentpath.substring(0, currentpath.lastIndexOf('/'));
	}

	@Override
	public void deleteDirectoryRecursive(String dirname) throws IOException {
		delete(dirname);
	}

	@Override
	public boolean isDirectory(String dirname) throws IOException {
		try {
			return dbxclient.getMetadata(getPath(dirname)).isFolder();
		} catch (DbxException e) {
			throw wrapException(e);
		}
	}

	@Override
	public void deleteDirectory(String dirname) throws IOException {
		delete(dirname);
	}

	@Override
	public void deleteFile(String name) throws IOException {
		delete(name);
	}

	@Override
	protected List<String> getFiles0() throws IOException {
		try {
			ArrayList<String> files = new ArrayList<String>();
			for (DbxEntry entry : dbxclient.getMetadataWithChildren(currentpath).children) {
				files.add(entry.name);
			}
			return files;
		} catch (DbxException e) {
			throw wrapException(e);
		}
	}

	@Override
	public void createFile(String name, InputStream inputsteam) throws IOException {
		try {
			dbxclient.uploadFileChunked(getPath(name), DbxWriteMode.force(), -1, new DbxStreamWriter.InputStreamCopier(inputsteam));
		} catch (DbxException e) {
			throw wrapException(e);
		}
	}


	private void delete(String name) throws IOException {
		try {
			dbxclient.delete(getPath(name));
		} catch (DbxException e) {
			throw wrapException(e);
		}
	}

	private String getPath(String name) {
		return currentpath + "/" + name;
	}


	static IOException wrapException(DbxException e) {
		return new IOException("Dbx error", e);
	}

}
