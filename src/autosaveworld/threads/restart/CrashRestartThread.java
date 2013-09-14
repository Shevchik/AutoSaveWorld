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

import autosaveworld.config.AutoSaveConfig;
import autosaveworld.core.AutoSaveWorld;

public class CrashRestartThread extends Thread{

	private AutoSaveWorld plugin;
	private AutoSaveConfig config;
	public CrashRestartThread(AutoSaveWorld plugin,AutoSaveConfig config)
	{
		this.plugin = plugin;
		this.config = config;
	}
	
	public void stopThread()
	{
		this.run = false;
	}
	
	private long syncticktime = 0;
	private boolean run = true;
	public void run()
	{
		plugin.debug("CrashRestartThread started");
		Thread.currentThread().setName("AutoSaveWorld CrashRestartThread");
		
		plugin.debug("Delaying crashrestart checker start for "+config.crdelay+" seconds");
		//wait for configurable delay
		try {Thread.sleep(config.crdelay*1000);} catch (InterruptedException e) {}

		plugin.debug("Running crashrestart checker");
		//schedule sync task in, this will provide us info about when the last server tick occured
		plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			public void run() {
				syncticktime = System.currentTimeMillis();
			}	
		}, 0, 20);
		
		while (run)
		{
			long diff = System.currentTimeMillis() - syncticktime;
			if (syncticktime !=0 && (diff >= (config.crtimeout*1000L))) 
			{
				run = false;

				if (config.crashrestartenabled) 
				{
					plugin.debug("Server has stopped responding. Probably this is a crash.");
					plugin.debug("Restarting Server");
					
					if (!config.crstop) 
					{
						plugin.JVMsh.setPath(config.crashrestartscriptpath);
						Runtime.getRuntime().addShutdownHook(plugin.JVMsh); 
					}
					
					plugin.getServer().shutdown();
					
				}
				
			}
			
			try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
		}
		
		plugin.debug("Graceful quit of CrashRestartThread");
		
	}
	
}

