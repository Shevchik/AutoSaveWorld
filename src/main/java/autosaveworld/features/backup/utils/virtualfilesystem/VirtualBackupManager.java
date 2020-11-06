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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.World;

import autosaveworld.core.GlobalConstants;
import autosaveworld.core.logging.MessageLogger;
import autosaveworld.features.backup.BackupUtils;
import autosaveworld.features.backup.InputStreamFactory;
import autosaveworld.features.backup.utils.PipedZip;

public class VirtualBackupManager {

	public static Builder builder() {
		return new Builder();
	}

	private String backuppath;
	private List<String> worlds;
	private boolean backupplugins;
	private List<String> otherfolders;
	private List<String> excludefolders;
	private int maxbackups;
	private boolean zip;
	private VirtualFileSystem vfs;

	private VirtualBackupManager(String backuppath, List<String> worlds, boolean backupplugins, List<String> otherfolders, List<String> excludefolders, int maxbackups, boolean zip, VirtualFileSystem vfs) {
		this.backuppath = backuppath;
		this.worlds = worlds;
		this.backupplugins = backupplugins;
		this.otherfolders = otherfolders;
		this.excludefolders = excludefolders;
		this.maxbackups = maxbackups;
		this.zip = zip;
		this.vfs = vfs;
	}

	public void backup() throws IOException {
		vfs.createAndEnterDirectory(backuppath);
		vfs.createAndEnterDirectory("backups");
		Set<String> backups = vfs.getEntries();
		if ((maxbackups != 0) && (backups.size() >= maxbackups)) {
			MessageLogger.debug("Finding oldest backup");
			String oldestBackup = BackupUtils.findOldestBackupName(backups);
			if (oldestBackup != null) {
				MessageLogger.debug("Deleting oldest backup " + oldestBackup);
				vfs.deleteDirectoryRecursive(oldestBackup);
			}
		}
		vfs.createAndEnterDirectory(new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(System.currentTimeMillis()));
		List<File> foldersToBackup = new ArrayList<>();
		for (World w : Bukkit.getWorlds()) {
			if (worlds.contains("*") || worlds.contains(w.getWorldFolder().getName())) {
				foldersToBackup.add(w.getWorldFolder());
			}
		}
		if (backupplugins) {
			foldersToBackup.add(GlobalConstants.getPluginsFolder());
		}
		for (String otherfolder : otherfolders) {
			foldersToBackup.add(new File(otherfolder));
		}
		for (File folder : foldersToBackup) {
			folder = folder.getAbsoluteFile();
			MessageLogger.debug("Backuping folder " + folder);
			try {
				backupFolder(folder.getAbsoluteFile());
			} catch (Exception e) {
				MessageLogger.exception("Failed to backup folder " + folder, e);
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
				if (BackupUtils.isFolderExcluded(excludefolders, dir.toString())) {
					return FileVisitResult.SKIP_SUBTREE;
				}
				vfs.createAndEnterDirectory(dir.getFileName().toString());
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				try (InputStream is = InputStreamFactory.getFileInputStream(file.toFile())) {
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
				vfs.leaveDirectory();
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
			MessageLogger.exception("Failed to backup file: " + filename, e);
			try {
				vfs.deleteFile(filename);
			} catch (IOException ex) {
			}
		}
	}


	public static class Builder {

		private String backuppath;
		private List<String> worlds;
		private boolean backupplugins;
		private List<String> otherfolders;
		private List<String> excludefolders;
		private int maxbackups;
		private boolean zip;
		private VirtualFileSystem vfs;

		private int allset;
		private boolean created;

		private Builder() {
		}

		public Builder setBackupPath(String backuppath) {
			check();
			this.backuppath = backuppath;
			allset |= 1;
			return this;
		}

		public Builder setWorldList(List<String> worlds) {
			check();
			this.worlds = new ArrayList<String>(worlds);
			allset |= 2;
			return this;
		}

		public Builder setBackupPlugins(boolean backupplugins) {
			check();
			this.backupplugins = backupplugins;
			allset |= 4;
			return this;
		}

		public Builder setOtherFolders(List<String> otherfoldes) {
			check();
			this.otherfolders = new ArrayList<String>(otherfoldes);
			allset |= 8;
			return this;
		}

		public Builder setExcludedFolders(List<String> excludedfolders) {
			check();
			this.excludefolders = new ArrayList<String>(excludedfolders);
			allset |= 16;
			return this;
		}

		public Builder setMaxBackupNumber(int max) {
			check();
			this.maxbackups = max;
			allset |= 32;
			return this;
		}

		public Builder setZip(boolean zip) {
			check();
			this.zip = zip;
			allset |= 64;
			return this;
		}

		public Builder setVFS(VirtualFileSystem vfs) {
			check();
			this.vfs = vfs;
			allset |= 128;
			return this;
		}

		public VirtualBackupManager create() {
			check();
			if (allset != 255) {
				throw new IllegalArgumentException("Not all options are set");
			}
			created = true;
			return new VirtualBackupManager(backuppath, worlds, backupplugins, otherfolders, excludefolders, maxbackups, zip, vfs);
		}

		private void check() {
			if (created) {
				throw new IllegalArgumentException("Object already created");
			}
		}
		
	}

}
