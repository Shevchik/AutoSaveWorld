/**
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 */

package autosaveworld.features.restart;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

import autosaveworld.utils.StringUtils;

public class RestartScript {

	public static File createScript(List<String> command) throws IOException {
		command = new ArrayList<String>(command);
		command.set(0, escape(command.get(0)));
		if (isUnix()) {
			return createUnixRestartScrpt(command);
		} else if (isWindows()) {
			return createWindowsRestartScript(command);
		} else {
			throw new PlatformNotSupportedException();
		}
	}

	private static File createUnixRestartScrpt(List<String> command) throws IOException {
		File file = new File("aswrestartscript.sh");
		file.createNewFile();
		try (PrintStream stream = new PrintStream(new FileOutputStream(file))) {
			stream.println("#!/bin/bash");
			stream.println("trap : SIGHUP");
			stream.println("while [ -d /proc/"+getPID()+" ]; do");
			stream.println("sleep 1");
			stream.println("done");
			stream.println("rm "+"\""+file.getAbsolutePath()+"\"");
			stream.println(StringUtils.join(command, " "));
		}
		file.setExecutable(true);
		return file;
	}

	private static File createWindowsRestartScript(List<String> command) throws IOException {
		File file = new File("aswrestartscript.bat");
		file.createNewFile();
		try (PrintStream stream = new PrintStream(new FileOutputStream(file))) {
			stream.println(":loop");
			stream.println("tasklist | find \" " + getPID() + " \" >nul");
			stream.println("if not errorlevel 1 (");
			stream.println("timeout /t 1 >nul");
			stream.println("goto :loop");
			stream.println(")");
			stream.println("del "+"\""+file.getAbsolutePath()+"\"");
			stream.println(StringUtils.join(command, " "));
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
		return File.separatorChar == '\\';
	}

	private static String getPID() {
		return ManagementFactory.getRuntimeMXBean().getName().split("[@]")[0];
	}

	protected static class PlatformNotSupportedException extends RuntimeException {
		private static final long serialVersionUID = 1L;
	}

}
