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
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;

public class JVMshutdownhook extends Thread {

	private String crashrestartscriptpath = "start.sh"; 
	
	public void setPath(String path)
	{
		this.crashrestartscriptpath = path;
	}
	
	
	public void restart()
	{
	try {
		File restartscript = new File(crashrestartscriptpath);
		String OS = System.getProperty("os.name").toLowerCase();
		if (restartscript.exists()) {
			System.out.println("[AutoSaveWorld] Startup script found. Restarting");	
			if (OS.contains("win")) {
				Runtime.getRuntime().exec("cmd /c start " + restartscript.getCanonicalPath());
			} else {
				Runtime.getRuntime().exec(restartscript.getCanonicalPath());
			}
		} else {
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
			
			ProcessBuilder pb = new ProcessBuilder();
			pb.command(execsequence);
			pb.start();

			}
	} catch (Exception e)
	{
		System.out.println("[AutoSaveWorld] Restart failed");
		e.printStackTrace();
	}
	
	}
	
	
	public void run()
	{
		if (!canRestart()) {System.out.println("[AutoSaveWorld] Waiting for WorldRegen shutdownhook to finish");}
		while (!canRestart())
		{
			try {
			Thread.sleep(1000);
			} catch (Exception e){
				e.printStackTrace();
			}
			
		}
		restart();
	}
	
	@SuppressWarnings("unchecked")
	boolean canRestart()
	{
		boolean b = true;
		try {
		    Class<?> hookclass = Class.forName("java.lang.ApplicationShutdownHooks");
		    Field field = hookclass.getDeclaredField("hooks");
		    field.setAccessible(true);
		    Map<Thread, Thread> hooks = (Map<Thread, Thread>) field.get(null);
		    for (Thread hook : hooks.keySet())
		    {
		    	if (hook.getName().equals("AutoSaveWorld WorldRegenShutdownHook"))
		    	{b=false;}
		    }
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return b;
	}
	
}
