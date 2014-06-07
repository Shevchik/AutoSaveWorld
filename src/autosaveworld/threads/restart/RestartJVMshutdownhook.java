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
import java.util.List;

import org.bukkit.Bukkit;

public class RestartJVMshutdownhook extends Thread {

	private String crashrestartscriptpath = "";

	public void setPath(String path) {
		this.crashrestartscriptpath = path;
	}


	public void restart() {
		//Wait if need
		while (RestartWaiter.shouldWait()) {
			try {Thread.sleep(1000);} catch (InterruptedException e) {}
		}
		//Force gc (attempt to ensure that the new instance of server will have enough mem)
		System.gc();
		System.gc();
		//Start process
		try {
			ProcessBuilder pb = new ProcessBuilder();
			File restartscript = new File(crashrestartscriptpath);
			if (!crashrestartscriptpath.isEmpty() && restartscript.exists()) {
				System.out.println("[AutoSaveWorld] Startup script found. Restarting");
				System.out.flush();
				restartscript.setExecutable(true);
				pb.command(restartscript.getAbsolutePath());
			} else {
				System.out.println("[AutoSaveWorld] Startup script not found. Restarting without it. This may work strange or not work at all");
				System.out.flush();
				String jarfilename = Bukkit.class.getResource("").getFile();
				jarfilename = jarfilename.substring(0, jarfilename.indexOf(".jar"));
				jarfilename = new File(jarfilename).getName()+".jar";
				List<String> arguments = ManagementFactory.getRuntimeMXBean().getInputArguments();
				List<String> execsequence = new ArrayList<String>();
				execsequence.add("java");
				execsequence.addAll(arguments);
				execsequence.add("-jar");
				execsequence.add(jarfilename);
				pb.command(execsequence);
			}
			pb.inheritIO();
			pb.start();
		} catch (Exception e) {
			System.out.println("[AutoSaveWorld] Restart failed");
			System.out.flush();
			e.printStackTrace();
		}
	}


	@Override
	public void run() {
		restart();
	}

}
