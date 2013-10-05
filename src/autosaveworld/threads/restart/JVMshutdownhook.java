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

import autosaveworld.threads.RestartWaiter;

public class JVMshutdownhook extends Thread {

	private RestartWaiter restartwaiter;
	public JVMshutdownhook(RestartWaiter restartwaiter)
	{
		this.restartwaiter = restartwaiter;
	}
	
	private String crashrestartscriptpath = ""; 

	public void setPath(String path)
	{
		this.crashrestartscriptpath = path;
	}
	
	
	public void restart()
	{
		try {
			ProcessBuilder pb = new ProcessBuilder();
			File restartscript = new File(crashrestartscriptpath);
			if (!crashrestartscriptpath.isEmpty() && restartscript.exists()) 
			{
				System.out.println("[AutoSaveWorld] Startup script found. Restarting");	
				restartscript.setExecutable(true);
				pb.command(restartscript.getPath());
			}
			else 
			{
				System.out.println("[AutoSaveWorld] Startup script not found. Restarting without it. This may work strange or not work at all");
				//requred info for start script
				String jarfilename = Bukkit.class.getResource("").getFile();
				jarfilename = jarfilename.substring(0, jarfilename.indexOf(".jar"));
				jarfilename = new File(jarfilename).getName()+".jar";
				List<String> arguments = ManagementFactory.getRuntimeMXBean().getInputArguments();			
				//start script building
				List<String> execsequence = new ArrayList<String>();
				execsequence.add("java");
				execsequence.addAll(arguments);
				execsequence.add("-jar");
				execsequence.add(jarfilename);
				pb.command(execsequence);
			}
			Process p = pb.start();
		
			//send IO to hell
			OutputThread output = new OutputThread(p);
			output.start();
			ErrorThread err = new ErrorThread(p);
			err.start();
		} catch (Exception e)
		{
			System.out.println("[AutoSaveWorld] Restart failed");
			e.printStackTrace();
		}
	}
	
	
	public void run()
	{
		if (!restartwaiter.canRestartNow())
		{
			System.out.println("Delaying restart");
			System.out.println("Reasons:");
			for (String reason : restartwaiter.getReasons())
			{
				System.out.println(reason);
			}
		}
		while (!restartwaiter.canRestartNow())
		{
			try {
			Thread.sleep(1000);
			} catch (Exception e){
				e.printStackTrace();
			}
		}
		restart();
	}
		
}
