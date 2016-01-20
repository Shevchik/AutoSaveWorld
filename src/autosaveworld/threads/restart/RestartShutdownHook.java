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

package autosaveworld.threads.restart;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;

import autosaveworld.core.logging.MessageLogger;
import autosaveworld.utils.StringUtils;

public class RestartShutdownHook extends Thread {

	public RestartShutdownHook() {
		RestartWaiter.init();
	}

	private boolean useAdvancedRestart = false;
	private File restartscript = null;

	public void setPath(String path) {
		restartscript = new File(path);
	}

	public void setUseAdvancedRestart(boolean b) {
		this.useAdvancedRestart = b;
	}

	public void restart() {
		while (RestartWaiter.shouldWait()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
		try {
			new ProcessBuilder()
			.command(useAdvancedRestart ? getAdvancedRestartCommand() : getBasicRestartCommand())
			.inheritIO()
			.start();
		} catch (Throwable e) {
			MessageLogger.printOutDebug("Restart failed");
			e.printStackTrace();
		}
	}

	private List<String> getAdvancedRestartCommand() {
		try {
			return Collections.singletonList(AdvancedRestartScript.createScript(
				restartScriptExists() ? restartscript.getAbsolutePath() : StringUtils.join(getJavaLaunchCommand().toArray(new String[0]), " ")
			).getAbsolutePath());
		} catch (Exception e) {
			return getBasicRestartCommand();
		}
	}

	private List<String> getBasicRestartCommand() {
		return restartScriptExists() ? Collections.singletonList(restartscript.getAbsolutePath()) : getJavaLaunchCommand();
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
