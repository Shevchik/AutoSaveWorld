package autosaveworld.threads.backup.sftp;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.bukkit.World;

import autosaveworld.core.GlobalConstants;
import autosaveworld.core.logging.MessageLogger;
import autosaveworld.zlibs.com.jcraft.jsch.ChannelSftp;
import autosaveworld.zlibs.com.jcraft.jsch.SftpException;

public class SFTPBackupOperations {

	private final boolean zip;
	private final List<String> excludefolders;
	private ChannelSftp sftp;
	public SFTPBackupOperations(ChannelSftp sftp, boolean zip, List<String> excludeFolders) {
		this.sftp = sftp;
		this.zip = zip;
		this.excludefolders = excludeFolders;
	}

	public void backupWorld(World world) {
		MessageLogger.debug("Backuping world "+world.getWorldFolder().getName());
		boolean savestaus = world.isAutoSave();
		world.setAutoSave(false);
		try {
			File worldfolder = world.getWorldFolder().getAbsoluteFile();
			backupFolder(worldfolder);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			world.setAutoSave(savestaus);
		}
		MessageLogger.debug("Backuped world "+world.getWorldFolder().getName());
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
			MessageLogger.debug("Backuping folder "+ folder);
			try {
				File fld = new File(folder).getAbsoluteFile();
				backupFolder(fld);
			} catch (Exception e) {
				e.printStackTrace();
			}
			MessageLogger.debug("Backuped folder "+ folder);
		}
	}

	private void backupFolder(File folder) throws IOException, SftpException {
		if (!zip) {
			SFTPUtils.uploadDirectory(sftp, folder, excludefolders);
		} else  {
			SFTPUtils.zipAndUploadDirectory(sftp, folder, excludefolders);
		}
	}

}
