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

import java.util.Calendar;
import java.util.logging.Logger;

import org.bukkit.Bukkit;


public class AutoRestartThread  extends Thread{
	private AutoSaveWorld plugin;
	private AutoSaveConfig config;
	AutoSaveConfigMSG configmsg;
	private boolean run = true;
	protected final Logger log = Bukkit.getLogger();

	AutoRestartThread(AutoSaveWorld plugin,AutoSaveConfig config,AutoSaveConfigMSG configmsg)
	{
		this.plugin = plugin;
		this.config = config;
		this.configmsg = configmsg;
	}
	
	public void stopthread()
	{
		this.run = false;
	}
	
	private String getCurTime()
	{
		Calendar cal = Calendar.getInstance();
		String curtime = 	cal.get(Calendar.HOUR_OF_DAY)+ ":"+  cal.get(Calendar.MINUTE);
		return curtime;
	}
	
	public void run()
	{	
		log.info("[AutoSaveWorld] AutoRestartThread started");
		Thread.currentThread().setName("AutoSaveWorld_AutoRestartThread");
		
		//check if we just restarted (server can restart faster than 1 minute, without this check, AutoRestartThread will stop working after restart)
		if  (config.autorestarttime.contains(getCurTime()))
			{
			//sleep for 1 minute
			try {Thread.sleep(61000);} catch (InterruptedException e) {e.printStackTrace();}
			}
		
		while (run)
		{
		//i know that this can be done using a java.util.timer, but i need a way to reload timer time
			if (config.autorestart)
			{
			 if (config.autorestarttime.contains(getCurTime()))
			 {
				if (config.autorestartcountdown) {
					for (int i = config.autorestartseconds; i>0; i--)
					{
						plugin.broadcast(configmsg.messageAutoRestartCountdown.replace("{SECONDS}", String.valueOf(i)));
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					} 
				}
				if (config.autorestartBroadcast) {plugin.broadcast(configmsg.messageAutoRestart);}
				log.info("[AutoSaveWorld] AutoRestarting server");
				plugin.getServer().dispatchCommand(Bukkit.getConsoleSender(), "stop");
				run = false;
				if (!config.astop) {
					plugin.JVMsh.setpath(config.autorestartscriptpath);
					Runtime.getRuntime().addShutdownHook(plugin.JVMsh); 
				}
			 }
			}
		try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
		}
		if (config.varDebug) {
			log.info(String.format("[%s] Graceful quit of AutoRestartThread", plugin.getDescription().getName()));
		}
		}
}
