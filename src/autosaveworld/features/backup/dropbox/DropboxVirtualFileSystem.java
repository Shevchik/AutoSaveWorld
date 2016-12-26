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

package autosaveworld.features.backup.dropbox;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import autosaveworld.features.backup.utils.virtualfilesystem.VirtualFileSystem;
import autosaveworld.utils.StringUtils;
import autosaveworld.zlibs.com.dropbox.core.DbxException;
import autosaveworld.zlibs.com.dropbox.core.v2.DbxClientV2;
import autosaveworld.zlibs.com.dropbox.core.v2.files.CommitInfo;
import autosaveworld.zlibs.com.dropbox.core.v2.files.FolderMetadata;
import autosaveworld.zlibs.com.dropbox.core.v2.files.GetMetadataErrorException;
import autosaveworld.zlibs.com.dropbox.core.v2.files.ListFolderResult;
import autosaveworld.zlibs.com.dropbox.core.v2.files.Metadata;
import autosaveworld.zlibs.com.dropbox.core.v2.files.UploadSessionCursor;
import autosaveworld.zlibs.com.dropbox.core.v2.files.UploadSessionStartResult;

public class DropboxVirtualFileSystem extends VirtualFileSystem {

	private final DbxClientV2 dbxclient;
	private final ArrayList<String> currentpath = new ArrayList<String>();
	public DropboxVirtualFileSystem(DbxClientV2 dbxclient) {
		this.dbxclient = dbxclient;
	}

	@Override
	public void enterDirectory0(String dirname) throws IOException {
		currentpath.add(dirname);
	}

	@Override
	public void createDirectory0(String dirname) throws IOException {
		try {
			dbxclient.files().createFolder(getPath(dirname));
		} catch (DbxException e) {
			throw wrapException(e);
		}
	}

	@Override
	public void leaveDirectory() throws IOException {
        if (currentpath.isEmpty()) {
        	throw new IOException("Can't leave root directory");
        }
        currentpath.remove(currentpath.size() - 1);
	}

	@Override
	public void deleteDirectoryRecursive(String dirname) throws IOException {
		delete(dirname);
	}

	@Override
	public boolean exists(String path) throws IOException {
		try {
			dbxclient.files().getMetadata(getPath(path));
			return true;
		} catch (GetMetadataErrorException e) {
			if (e.errorValue.isPath() && e.errorValue.getPathValue().isNotFound()) {
				return false;
			} else {
				throw wrapException(e);
			}
		} catch (DbxException e) {
			throw wrapException(e);
		}
	}

	@Override
	public boolean isDirectory(String dirname) throws IOException {
		try {
			return dbxclient.files().getMetadata(getPath(dirname)) instanceof FolderMetadata;
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
	public Set<String> getEntries() throws IOException {
		try {
			HashSet<String> files = new HashSet<String>();
			String path = getPath(null);

			ListFolderResult result = dbxclient.files().listFolder(path);
			while (true) {
				for (Metadata metadata : result.getEntries()) {
					files.add(metadata.getName());
				}
				if (!result.getHasMore()) {
					break;
				}
				result = dbxclient.files().listFolderContinue(result.getCursor());
			}
			return files;
		} catch (DbxException e) {
			throw wrapException(e);
		}
	}

	@Override
	public void createFile(String name, InputStream inputsteam) throws IOException {
		try {
			byte[] buffer = new byte[1024 * 1024 * 20];
			long totalBytesRead = inputsteam.read(buffer);
			UploadSessionStartResult res = dbxclient.files().uploadSessionStart().uploadAndFinish(new ByteArrayInputStream(buffer, 0, (int) totalBytesRead));
			String sessionId = res.getSessionId();
			int bytesRead = -1;
			while ((bytesRead = inputsteam.read(buffer)) != -1) {
				dbxclient.files().uploadSessionAppendV2(new UploadSessionCursor(sessionId, totalBytesRead)).uploadAndFinish(new ByteArrayInputStream(buffer, 0, bytesRead));
				totalBytesRead += bytesRead;
			}
			dbxclient.files().uploadSessionFinish(new UploadSessionCursor(sessionId, totalBytesRead), new CommitInfo(getPath(name))).finish();
		} catch (DbxException e) {
			throw wrapException(e);
		}
	}


	private void delete(String name) throws IOException {
		try {
			dbxclient.files().delete(getPath(name));
		} catch (DbxException e) {
			throw wrapException(e);
		}
	}

	private String getPath(String name) {
		ArrayList<String> fullpath = new ArrayList<>(currentpath);
		if (!StringUtils.isNullOrEmpty(name)) {
			fullpath.add(name);
		}
		return "/" + StringUtils.join(fullpath, "/");
	}


	static IOException wrapException(DbxException e) {
		return new IOException("Dbx error", e);
	}

}
