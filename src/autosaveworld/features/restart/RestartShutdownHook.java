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
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;

import autosaveworld.core.logging.MessageLogger;
import autosaveworld.features.restart.RestartScript.PlatformNotSupportedException;

public class RestartShutdownHook extends Thread {

	private final File restartscript;
	public RestartShutdownHook(File restartscript) {
		this.restartscript = restartscript;
	}

	public void restart() {
		RestartWaiter.await();
		try {
			new ProcessBuilder()
			.command(getRestartCommand())
			.inheritIO()
			.start();
		} catch (Throwable e) {
			MessageLogger.printOut("Restart failed");
			MessageLogger.printOutException(e);
		}
	}

	private List<String> getRestartCommand() throws IOException {
		try {
			return Collections.singletonList(RestartScript.createScript(
				restartScriptExists() ? Collections.singletonList(restartscript.getAbsolutePath()) : getJavaLaunchCommand()
			).getAbsolutePath());
		} catch (IOException | PlatformNotSupportedException e) {
			if (restartScriptExists()) {
				return Collections.singletonList(restartscript.getAbsolutePath());
			} else {
				throw new RuntimeException("Unable to create temporal restart script and server start script doesn't exist", e);
			}
		}
	}

	private List<String> getJavaLaunchCommand() {
		String jarfilename = Bukkit.class.getResource("").getFile();
		jarfilename = jarfilename.substring(0, jarfilename.indexOf(".jar"));
		jarfilename = new File(jarfilename).getName() + ".jar";
		List<String> command = new ArrayList<String>();
		command.add("java");
		command.addAll(ManagementFactory.getRuntimeMXBean().getInputArguments());
		command.add("-jar");
		command.add(jarfilename);
		return command;
	}

	private boolean restartScriptExists() {
		return restartscript != null && restartscript.exists() && restartscript.isFile();
	}

	@Override
	public void run() {
		restart();
	}

}
