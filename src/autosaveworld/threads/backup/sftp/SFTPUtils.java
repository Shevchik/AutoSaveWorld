package autosaveworld.threads.backup.sftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Vector;

import autosaveworld.threads.backup.ExcludeManager;
import autosaveworld.threads.backup.utils.MemoryZip;
import autosaveworld.zlibs.com.jcraft.jsch.ChannelSftp;
import autosaveworld.zlibs.com.jcraft.jsch.ChannelSftp.LsEntry;
import autosaveworld.zlibs.com.jcraft.jsch.SftpException;

public class SFTPUtils {

	public static void uploadDirectory(ChannelSftp sftp, File src, List<String> excludefolders) throws SftpException {
		if (src.isDirectory()) {
			sftp.mkdir(src.getName());
			sftp.cd(src.getName());
			for (File file : src.listFiles()) {
				if (!ExcludeManager.isFolderExcluded(excludefolders, file.getPath())) {
					uploadDirectory(sftp, file, excludefolders);
				}
			}
			sftp.cd("..");
		} else {
			if (!src.getName().endsWith(".lck")) {
				try {
					InputStream is = new FileInputStream(src);
					storeFile(sftp, is, src.getName()+".zip");
					is.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void zipAndUploadDirectory(ChannelSftp sftp, File src, List<String> excludefolders) throws IOException {
		InputStream is = MemoryZip.startZIP(src, excludefolders);
		storeFile(sftp, is, src.getName()+".zip");
		is.close();
	}

	private static void storeFile(ChannelSftp sftp, InputStream is, String filename) {
		try {
			sftp.put(is, filename);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Thread.yield();
	}

	public static void deleteDirectory(ChannelSftp sftp, String oldestBackup) throws SftpException {
		Vector<LsEntry> entries = sftp.ls(oldestBackup);
		for (LsEntry entry : entries) {
			if (entry.getAttrs().isDir()) {
				sftp.cd(entry.getFilename());
				deleteDirectory(sftp, entry.getFilename());
				sftp.cd("..");
				sftp.rmdir(entry.getFilename());
			} else {
				sftp.rm(entry.getFilename());
			}
		}
	}

}
