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

import java.text.SimpleDateFormat;
import java.util.logging.Logger;

import org.bukkit.Bukkit;

import autosaveworld.config.AutoSaveConfig;
import autosaveworld.config.AutoSaveConfigMSG;
import autosaveworld.core.AutoSaveWorld;


public class AutoRestartThread  extends Thread{
	private AutoSaveWorld plugin;
	private AutoSaveConfig config;
	AutoSaveConfigMSG configmsg;
	private boolean run = true;
	protected final Logger log = Bukkit.getLogger();
	SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

	
	public AutoRestartThread(AutoSaveWorld plugin,AutoSaveConfig config,AutoSaveConfigMSG configmsg)
	{
		this.plugin = plugin;
		this.config = config;
		this.configmsg = configmsg;
	}
	
	public void stopThread()
	{
		this.run = false;
	}
	
	private boolean command = false;
	public void startrestart()
	{
		this.command = true;
	}
	

	
	public void run()
	{	
		log.info("[AutoSaveWorld] AutoRestartThread started");
		Thread.currentThread().setName("AutoSaveWorld AutoRestartThread");
		
		//check if we just restarted (server can restart faster than 1 minute. Without this check AutoRestartThread will stop working after restart)
		if  (config.autorestarttime.contains(getCurTime()))	{try {Thread.sleep(61000);} catch (InterruptedException e) {}}
		
		while (run)
		{
		//i know that this can be done using a java.util.timer, but i need a way to reload timer time
			 if ((config.autorestart && config.autorestarttime.contains(getCurTime())) || command)
			 {
				run = false;
				
				if (config.autorestartcountdown) {
					for (int i = config.autorestartseconds; i>0; i--)
					{
						plugin.broadcast(configmsg.messageAutoRestartCountdown.replace("{SECONDS}", String.valueOf(i)));
						try {Thread.sleep(1000);} catch (InterruptedException e) {}
					} 
				}
				
				if (config.autorestartBroadcast) {
					plugin.broadcast(configmsg.messageAutoRestart);
				}
				
				plugin.debug("[AutoSaveWorld] AutoRestarting server");
				
				if (!config.astop) {
					plugin.JVMsh.setPath(config.autorestartscriptpath);
					Runtime.getRuntime().addShutdownHook(plugin.JVMsh); 
				}
				
				plugin.getServer().shutdown();
			}
		try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
		}
		
		if (config.varDebug) {log.info("[AutoSaveWorld] Graceful quit of AutoRestartThread");}
	}
	
	
	
	private String getCurTime()
	{
		String curtime = sdf.format(System.currentTimeMillis());
		return curtime;
	}
	
}
