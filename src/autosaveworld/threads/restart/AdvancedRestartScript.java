package autosaveworld.threads.restart;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

import autosaveworld.utils.StringUtils;

public class AdvancedRestartScript {

	public static File createScript(List<String> command) throws IOException {
		command = new ArrayList<String>(command);
		command.set(0, escape(command.get(0)));
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
				stream.println(StringUtils.join(command, " "));
			} else if (isWindows()) {
				stream.println(":loop");
				stream.println("tasklist | find \" " + getPID() + " \" >nul");
				stream.println("if not errorlevel 1 (");
				stream.println("imeout /t 1 >nul");
				stream.println("goto :loop");
				stream.println(")");
				stream.println("del "+"\""+file.getAbsolutePath()+"\"");
				stream.println(StringUtils.join(command, " "));
			} else {
				throw new PlatformNotSupportedException();
			}
		}
		file.setExecutable(true);
		return file;
	}

	private static String escape(String string) {
		return "\""+string+"\"";
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
