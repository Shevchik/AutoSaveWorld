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

package autosaveworld;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.fusesource.jansi.Ansi.Color;

public class CrashRestartThread extends Thread{

	private AutoSaveWorld plugin;
	private AutoSaveConfig config;
	private boolean run = true;
	protected final Logger log = Bukkit.getLogger();
	private long syncticktime = 0;
	
	CrashRestartThread(AutoSaveWorld plugin,AutoSaveConfig config)
	{
		this.plugin = plugin;
		this.config = config;
	}
	
	public void stopthread()
	{
		this.run = false;
	}
	
	
	public void run()
	{
		log.info("[AutoSaveWorld] CrashRestartThread started");
		Thread.currentThread().setName("AutoSaveWorld_CrashRestartThread");
		int tasknumber = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			public void run() {
				syncticktime = System.currentTimeMillis();
				if (config.crdebug) {
				plugin.debug("CrashRestartThread SyncTicktime: "+syncticktime);
				}
			}	
		}, 0, 20);
		while (run)
		{
			long diff = System.currentTimeMillis() - syncticktime;
			if (syncticktime !=0 && (diff >= (config.crtimeout*1000L))) 
			{log.info("[AutoSaveWorld]"+Color.RED+"Server has stopped responding. Probably this is a crash.");
			run = false;
				if (config.crashrestartenabled) {
				log.info("[AutoSaveWorld] CrashRestart is enabled, AutoSaveWorld will try to restart server");
				if (!config.crstop) {
				plugin.JVMsh.setpath(config.crashrestartscriptpath);
				Runtime.getRuntime().addShutdownHook(plugin.JVMsh); 
				log.info("[AutoSaveWorld]Restarting server.");} else
				{log.info("[AutoSaveWorld]Just stopping server.");}
				plugin.getServer().dispatchCommand(Bukkit.getConsoleSender(), "stop");
				} else {
				log.info("[AutoSaveWorld] CrashRestart is disabled, AutoSaveWorld won't try to restart server");
				}
			}
			if (config.crdebug) {
				plugin.debug("CrashRestartThread ASyncTicktime: "+System.currentTimeMillis());
				plugin.debug("CrashRestartThread diff: "+diff);
				}
			try {Thread.sleep(999);} catch (InterruptedException e) {e.printStackTrace();}
		}
		plugin.getServer().getScheduler().cancelTask(tasknumber);
		if (config.varDebug) {
			log.info(String.format("[%s] Graceful quit of CrashRestartThread", plugin.getDescription().getName()));
		}
	}
}

