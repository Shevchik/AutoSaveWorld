/**
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * 
 */

package autosaveworld.threads.consolecommand;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;

import autosaveworld.config.AutoSaveConfig;
import autosaveworld.core.AutoSaveWorld;

public class AutoConsoleCommandThread extends Thread {

	private AutoSaveWorld plugin = null;
	private AutoSaveConfig config;
	public AutoConsoleCommandThread(AutoSaveWorld plugin, AutoSaveConfig config) {
		this.plugin = plugin;
		this.config = config;
	}
	
	public void stopThread()
	{
		this.run = false;
	}
	
	private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
	private SimpleDateFormat msdf = new SimpleDateFormat("mm");
	private long loaded = 0;
	
	private volatile boolean run = true;
	public void run() {

		plugin.debug("AutoConsoleCommandThread Started");
		Thread.currentThread().setName("AutoSaveWorld AutoConsoleCommandThread");
		
		//wait for server to start
		int ltask = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
		{
			public void run()
			{
				loaded = System.currentTimeMillis();
			}
		});
		while (loaded == 0) {
			try{Thread.sleep(1000);} catch (Exception e) {e.printStackTrace();}
		}
		Bukkit.getScheduler().cancelTask(ltask);
		
		
		while (run) {
			
			//handle times mode
			if (config.cctimeenabled) 
			{
				int cminute = Integer.valueOf(msdf.format(System.currentTimeMillis()));
				String ctime = getCurTime();
				if (cminute != minute && config.cctimetimes.contains(ctime))
				{
					plugin.debug("Executing console commands (timesmode)");
					minute = cminute;
					executeCommands(config.cctimescommands.get(ctime));
				}
			}
			
			//handle interval mode
			if (config.ccintervalenabled)
			{
				for (int interval : getIntervalsToExecute()) 
				{
					plugin.debug("Executing console commands (intervalmode)");
					executeCommands(config.ccintervalscommands.get(interval));
				}
				intervalcounter++;
			}
			
			//sleep for a second
			try {Thread.sleep(1000);} catch (InterruptedException e) {}
		}
		
		plugin.debug("Graceful quit of AutoConsoleCommandThread");

	}
	

	private void executeCommands(final List<String> commands)
	{
		if (plugin != null && plugin.isEnabled())
		{
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
			{
				public void run()
				{
					ConsoleCommandSender csender = Bukkit.getConsoleSender();
					for (String command : commands)
					{
						Bukkit.dispatchCommand(csender, command);
					}
				}
			});
		}
	}
	
	
	//timesmode checks (to executed command only once per minute)
	private int minute = -1;
	private String getCurTime()
	{
		return sdf.format(System.currentTimeMillis());
	}
	
	//intervalmode checks (to know when we last executed interval command)
	private long intervalcounter = 0;
	private List<Integer> getIntervalsToExecute()
	{
		List<Integer> inttoexecute = new ArrayList<Integer>();
		for (int interval : config.ccintervalstimes)
		{
			if (intervalcounter % interval == 0)
			{
				inttoexecute.add(interval);
			}
		}
		return inttoexecute;
	}
	
	
}
