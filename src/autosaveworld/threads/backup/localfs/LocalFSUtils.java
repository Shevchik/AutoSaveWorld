package autosaveworld.threads.backup.localfs;

import java.io.File;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.List;

import autosaveworld.threads.backup.ExcludeManager;

public class LocalFSUtils {

	public static void copyDirectory(File sourceLocation , File targetLocation, List<String> excludefolders) {
		if (sourceLocation.isDirectory()) {
			if (!targetLocation.exists()) {
				targetLocation.mkdirs();
			}
			for (String filename : sourceLocation.list()) {
				if (!ExcludeManager.isFolderExcluded(excludefolders, new File(sourceLocation, filename).getPath())) {
					copyDirectory(new File(sourceLocation, filename), new File(targetLocation, filename), excludefolders);
				}
			}
		} else {
			if (!sourceLocation.getName().endsWith(".lck")) {
				try {
					Files.copy(sourceLocation.toPath(), targetLocation.toPath());
				} catch (Exception e) {
					e.printStackTrace();
				}
				Thread.yield();
			}
		}
	}

	public static String findOldestBackupName(String backupsfodler) {
		String[] timestamps = new File(backupsfodler).list();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
		String oldestBackupName = null;
		long old = System.currentTimeMillis();
		for (String timestampString : timestamps) {
			try {
				long cur = System.currentTimeMillis();
				if (timestampString.endsWith(".zip")) {
					cur = sdf.parse(timestampString.substring(0,timestampString.indexOf(".zip"))).getTime();
				} else {
					cur = sdf.parse(timestampString).getTime();
				}
				if (cur < old) {
					old = cur;
					oldestBackupName = timestampString;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return oldestBackupName;
	}

}
