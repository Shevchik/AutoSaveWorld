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

import org.bukkit.Bukkit;

import autosaveworld.config.AutoSaveConfig;
import autosaveworld.config.AutoSaveConfigMSG;
import autosaveworld.core.AutoSaveWorld;


public class AutoRestartThread  extends Thread{

	private AutoSaveWorld plugin;
	private AutoSaveConfig config;
	private AutoSaveConfigMSG configmsg;
	private RestartJVMshutdownhook jvmsh;
	public AutoRestartThread(AutoSaveWorld plugin, AutoSaveConfig config, AutoSaveConfigMSG configmsg, RestartJVMshutdownhook jvmsh)
	{
		this.plugin = plugin;
		this.config = config;
		this.configmsg = configmsg;
		this.jvmsh = jvmsh;
	}

	public void stopThread()
	{
		this.run = false;
	}

	public void startrestart(boolean skipcountdown)
	{
		this.command = true;
	}


	private volatile boolean run = true;
	private boolean command = false;
	private boolean skipcountdown = false;
	@Override
	public void run()
	{
		plugin.debug("AutoRestartThread started");
		Thread.currentThread().setName("AutoSaveWorld AutoRestartThread");

		//check if we just restarted (server can restart faster than 1 minute. Without this check AutoRestartThread will stop working after restart)
		if  (config.autorestarttime.contains(getCurTime()))	{try {Thread.sleep(61000);} catch (InterruptedException e) {}}

		while (run)
		{
			if ((config.autorestart && config.autorestarttime.contains(getCurTime())) || command)
			{
				run = false;
				command = false;

				if (config.autorestartcountdown && !skipcountdown)
				{
					for (int i = config.autorestartbroadcastonseconds.get(0); i>0; i--)
					{
						if (config.autorestartbroadcastonseconds.contains(i))
						{
							plugin.broadcast(configmsg.messageAutoRestartCountdown.replace("{SECONDS}", String.valueOf(i)), true);
						}
						try {Thread.sleep(1000);} catch (InterruptedException e) {}
					}
				}

				plugin.broadcast(configmsg.messageAutoRestart, config.autorestartBroadcast);

				plugin.debug("AutoRestarting server");

				if (!config.astop)
				{
					jvmsh.setPath(config.autorestartscriptpath);
					Runtime.getRuntime().addShutdownHook(jvmsh);
				}

				
				int taskid = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
				{
					@Override
					public void run()
					{
						for (String command : config.autorestartcommmands)
						{
							Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
						}
					}
				});
				int curwait = 0;
				while ((Bukkit.getScheduler().isCurrentlyRunning(taskid) || Bukkit.getScheduler().isQueued(taskid)) && curwait < 10)
				{
					try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
					curwait++;
				}

				plugin.getServer().shutdown();

			}
			try {Thread.sleep(1000);} catch (InterruptedException e) {}
		}

		plugin.debug("Graceful quit of AutoRestartThread");

	}

	SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
	private String getCurTime()
	{
		String curtime = sdf.format(System.currentTimeMillis());
		return curtime;
	}

}
