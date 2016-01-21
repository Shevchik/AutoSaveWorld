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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import org.bukkit.World;

import autosaveworld.core.GlobalConstants;
import autosaveworld.core.logging.MessageLogger;
import autosaveworld.features.backup.ExcludeManager;
import autosaveworld.features.backup.InputStreamConstruct;
import autosaveworld.features.backup.utils.PipedZip;

public class VirutialBackupOperations {

	private VirtualFileSystem vfs;
	private boolean zip;
	private List<String> excludefolders;

	public VirutialBackupOperations(VirtualFileSystem vfs, boolean zip, List<String> excludefolders) {
		this.vfs = vfs;
		this.zip = zip;
		this.excludefolders = excludefolders;
	}

	public void backupWorld(World world) {
		MessageLogger.debug("Backuping world " + world.getWorldFolder().getName());
		try {
			File worldfolder = world.getWorldFolder().getAbsoluteFile();
			backupFolder(worldfolder);
		} catch (Exception e) {
			e.printStackTrace();
		}
		MessageLogger.debug("Backuped world " + world.getWorldFolder().getName());
	}

	public void backupPlugins() {
		try {
			File plfolder = new File(GlobalConstants.getPluginsFolder()).getAbsoluteFile();
			backupFolder(plfolder);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void backupOtherFolders(List<String> folders) {
		for (String folder : folders) {
			MessageLogger.debug("Backuping folder " + folder);
			try {
				File fld = new File(folder).getAbsoluteFile();
				backupFolder(fld);
			} catch (Exception e) {
				e.printStackTrace();
			}
			MessageLogger.debug("Backuped folder " + folder);
		}
	}

	private void backupFolder(File folder) throws IOException {
		if (!zip) {
			uploadDirectory(folder);
		} else {
			zipAndUploadDirectory(folder);
		}
	}


	private void uploadDirectory(File src)  throws IOException {
		Files.walkFileTree(src.toPath(), new FileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				if (ExcludeManager.isFolderExcluded(excludefolders, dir.toString())) {
					return FileVisitResult.SKIP_SUBTREE;
				}
				vfs.createAndChangeDirectory(dir.getFileName().toString());
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				try (InputStream is = InputStreamConstruct.getFileInputStream(file.toFile())) {
					storeFile(is, file.getFileName().toString());
				}
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				vfs.changeToParentDirectiry();
				return FileVisitResult.CONTINUE;
			}
		});
	}

	private void zipAndUploadDirectory(File src) throws IOException {
		try (InputStream is = PipedZip.startZIP(src, excludefolders)) {
			storeFile(is, src.getName() + ".zip");
		}
	}

	private void storeFile(InputStream is, String filename) {
		try {
			vfs.createFile(filename, is);
		} catch (IOException e) {
			MessageLogger.warn("Failed to backup file: " + filename);
			try {
				vfs.deleteFile(filename);
			} catch (IOException ex) {
			}
		}
	}

}
