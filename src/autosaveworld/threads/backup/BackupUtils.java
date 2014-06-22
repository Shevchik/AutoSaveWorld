package autosaveworld.threads.backup;

import java.text.SimpleDateFormat;

public class BackupUtils {

	public static String findOldestBackupName(String[] timestamps) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
		String oldestBackupName = null;
		long old = System.currentTimeMillis();
		for (String timestampString : timestamps) {
			try {
				long cur = System.currentTimeMillis();
				if (timestampString.endsWith(".zip")) {
					cur = sdf.parse(timestampString.substring(0, timestampString.indexOf(".zip"))).getTime();
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
