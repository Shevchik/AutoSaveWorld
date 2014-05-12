package autosaveworld.threads.backup.utils;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import autosaveworld.threads.backup.utils.memorystream.MemoryInputStream;
import autosaveworld.threads.backup.utils.memorystream.MemoryStream;

public class MemoryZip {

	private static ExecutorService executor = Executors.newSingleThreadExecutor();
	public static MemoryInputStream startZIP(final File inputDir, final List<String> excludefolders) {
		final MemoryStream mz = new MemoryStream();
		executor.submit(
			new Runnable() {
				@Override
				public void run() {
					ZipUtils.zipFolder(inputDir, mz.getOutputStream(), excludefolders);
					mz.putStreamEndSignal();
				}
			}
		);
		executor.shutdown();
		return mz.getInputStream();
	}

}
