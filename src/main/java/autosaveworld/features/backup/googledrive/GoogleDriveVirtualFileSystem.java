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

package autosaveworld.features.backup.googledrive;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import autosaveworld.features.backup.utils.virtualfilesystem.VirtualFileSystem;
import autosaveworld.zlibs.com.google.api.client.http.InputStreamContent;
import autosaveworld.zlibs.com.google.api.services.drive.Drive;
import autosaveworld.zlibs.com.google.api.services.drive.model.File;
import autosaveworld.zlibs.com.google.api.services.drive.model.ParentReference;

public class GoogleDriveVirtualFileSystem extends VirtualFileSystem {

	private static final String folderMimeType = "application/vnd.google-apps.folder";

	private final Drive driveclient;
	private final String rootfolder;
	private ArrayList<String> currentpath = new ArrayList<>();
	public GoogleDriveVirtualFileSystem(Drive driveclient, String rootfolder) {
		this.driveclient = driveclient;
		this.rootfolder = rootfolder;
	}

	@Override
	protected void enterDirectory0(String dirname) throws IOException {
		File file = findFile(dirname);
		if (file == null) {
			throw new IOException(dirname + " doesn't exist");
		}
		if (!file.getMimeType().equalsIgnoreCase(folderMimeType)) {
			throw new IOException(dirname + " is not a folder (wrong mime-type " + file.getMimeType() + ")");
		}
		currentpath.add(file.getId());
	}

	@Override
	protected void createDirectory0(String dirname) throws IOException {
		File folder = new File();
		folder.setTitle(dirname);
		folder.setMimeType("application/vnd.google-apps.folder");
		folder.setParents(Collections.singletonList(new ParentReference().setId(getCurrentFolder())));
		driveclient.files().insert(folder).execute();
	}

	@Override
	public void leaveDirectory() throws IOException {
		if (currentpath.isEmpty()) {
			throw new IOException("Can't leave root directory");
		}
        currentpath.remove(currentpath.size() - 1);
	}

	@Override
	public boolean exists(String path) throws IOException {
		 return findFile(path) != null;
	}

	@Override
	public boolean isDirectory(String dirname) throws IOException {
		File file = findFile(dirname);
		if (file == null) {
			throw new IOException(dirname + " doesn't exist");
		}
		return file.getMimeType().equalsIgnoreCase(folderMimeType);
	}

	@Override
	public void deleteDirectoryRecursive(String dirname) throws IOException {
		delete(dirname);
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
		HashSet<String> result = new HashSet<>();
		for (File file : driveclient.files().list().setQ(quotes(getCurrentFolder()) + " in parents").execute().getItems()) {
			result.add(file.getTitle());
		}
		return result;
	}

	@Override
	public void createFile(String name, InputStream inputsteam) throws IOException {
		File file = new File();
		file.setTitle(name);
		file.setParents(Collections.singletonList(new ParentReference().setId(getCurrentFolder())));
		driveclient.files().insert(file, new InputStreamContent(null, inputsteam)).execute();
	}


	private void delete(String name) throws IOException {
		File file = findFile(name);
		if (file == null) {
			return;
		}
		driveclient.files().delete(file.getId()).execute();
	}

	private File findFile(String dirname) throws IOException {
		List<File> files = driveclient.files().list().setQ(quotes(getCurrentFolder()) + " in parents and title = " +  quotes(dirname)).execute().getItems();
		if (files.isEmpty()) {
			return null;
		}
		return files.get(0);
	}

	private String getCurrentFolder() {
		return currentpath.isEmpty() ? rootfolder : currentpath.get(currentpath.size() - 1);
	}

	private static String quotes(String str) {
		return "'" + str + "'";
	}

}
