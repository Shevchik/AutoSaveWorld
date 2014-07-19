package autosaveworld.threads.backup.localfs;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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
					Files.copy(sourceLocation.toPath(), targetLocation.toPath(), StandardCopyOption.REPLACE_EXISTING);
				} catch (Exception e) {
					e.printStackTrace();
				}
				try {Thread.sleep(0);} catch (InterruptedException e) {}
			}
		}
	}

}
