package autosaveworld.threads.restart;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;

public class AdvancedRestartScript {

	public static File createScript(String command) throws IOException {
		File file = new File("aswrestartscript");
		file.createNewFile();
		try (PrintStream stream = new PrintStream(new FileOutputStream(file))) {
			if (isUnix()) {
				stream.println("#!/bin/bash");
				stream.println("trap : SIGHUP");
				stream.println("while [ -d /proc/"+getPID()+" ]; do");
				stream.println("sleep 1");
				stream.println("done");
				stream.println("rm "+"\""+file.getAbsolutePath()+"\"");
				stream.println("\""+command+"\"");
			} else if (isWindows()) {
				stream.println(":loop");
				stream.println("tasklist | find \" " + getPID() + " \" >nul");
				stream.println("if not errorlevel 1 (");
				stream.println("imeout /t 1 >nul");
				stream.println("goto :loop");
				stream.println(")");
				stream.println("del "+"\""+file.getAbsolutePath()+"\"");
				stream.println("\""+command+"\"");
			} else {
				throw new PlatformNotSupportedException();
			}
		}
		file.setExecutable(true);
		return file;
	}

	private static boolean isUnix() {
		return File.separatorChar == '/';
	}

	private static boolean isWindows() {
		return File.pathSeparatorChar == '\\';
	}

	private static String getPID() {
		return ManagementFactory.getRuntimeMXBean().getName().split("[@]")[0];
	}

	protected static class PlatformNotSupportedException extends RuntimeException {
		private static final long serialVersionUID = 1L;
	}

}
