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

package autosaveworld.threads.save;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitScheduler;

import autosaveworld.core.AutoSaveWorld;
import autosaveworld.config.AutoSaveConfig;
import autosaveworld.config.AutoSaveConfigMSG;

public class AutoSaveThread extends Thread {


	private AutoSaveWorld plugin = null;
	private AutoSaveConfig config;
	private AutoSaveConfigMSG configmsg;
	public AutoSaveThread(AutoSaveWorld plugin, AutoSaveConfig config, AutoSaveConfigMSG configmsg) {
		this.plugin = plugin;
		this.config = config;
		this.configmsg = configmsg;
	}


	public void stopThread() 
	{
		this.run = false;
	}

	public void startsave() 
	{
		if (plugin.saveInProgress)
		{
			plugin.warn("Multiple concurrent saves attempted! Save interval is likely too short!");
			return;
		}
		command = true;
		i = config.saveInterval;
	}
	
	// The code to run...weee
	private int i;
	private volatile boolean run = true;
	private boolean command = false;
	public void run() 
	{

		plugin.debug("AutoSaveThread Started");
		Thread.currentThread().setName("AutoSaveWorld AutoSaveThread");
		
		while (run) {
			// Prevent AutoSave from never sleeping
			// If interval is 0, sleep for 5 seconds and skip saving
			if(config.saveInterval == 0) {
				try {Thread.sleep(5000);} catch(InterruptedException e) {}
				continue;
			}
			
			//sleep
			for (i = 0; i < config.saveInterval; i++) {
				if (!run) {break;}
				try {Thread.sleep(1000);} catch (InterruptedException e) {}
			}

			//save
			if (run&&(config.saveEnabled||command)) {
				performSave();
			}
		}
		
		plugin.debug("Graceful quit of AutoSaveThread");

	}
	
	
	
	
	private void savePlayers() 
	{
		if (plugin.isEnabled())
		{
			plugin.debug("Saving players");
			BukkitScheduler scheduler = Bukkit.getScheduler();
			int taskid = scheduler.scheduleSyncDelayedTask(plugin, new Runnable()
			{
				public void run()
				{
					Bukkit.savePlayers();
				}
			});
			while (scheduler.isCurrentlyRunning(taskid) || scheduler.isQueued(taskid))
			{
				try {Thread.sleep(100);} catch (InterruptedException e) {}
			}
			plugin.debug("Saved Players");
		}
	}

	private void saveWorlds() 
	{
		plugin.debug("Saving worlds");
		BukkitScheduler scheduler = Bukkit.getScheduler();
		for (final World world : plugin.getServer().getWorlds()) 
		{
			if (plugin.isEnabled())
			{
				plugin.debug(String.format("Saving world: %s", world.getName()));
				int taskid = scheduler.scheduleSyncDelayedTask(plugin, new Runnable()
				{
					public void run()
					{
					world.save();
					}
				});
				while (scheduler.isCurrentlyRunning(taskid) || scheduler.isQueued(taskid))
				{
					try {Thread.sleep(100);} catch (InterruptedException e) {}
				}
			}
		}
		plugin.debug("Saved Worlds");
	}

	private void performSave() 
	{
		if (plugin.getServer().getOnlinePlayers().length == 0 && !command) {
			// No players online, don't bother saving.
			plugin.debug("Skipping save, no players online.");
			return;
		}
		
		command = false;

		if (plugin.backupInProgress) {
			plugin.warn("AutoBackup is in process. AutoSave cancelled");
			return;
		}
		
		// Lock
		plugin.saveInProgress = true;

		plugin.broadcast(configmsg.messageSaveBroadcastPre, config.saveBroadcast);

		// Save the players
		savePlayers();
		
		// Save the worlds
		saveWorlds();

		plugin.broadcast(configmsg.messageSaveBroadcastPost, config.saveBroadcast);

		plugin.LastSave =new java.text.SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(System.currentTimeMillis());

		// Release
		plugin.saveInProgress = false;
	}

	public void performSaveForce()
	{
		plugin.saveInProgress = true;
		plugin.broadcast(configmsg.messageSaveBroadcastPre, config.saveBroadcast);
		plugin.debug("Saving players");
		plugin.getServer().savePlayers();
		plugin.debug("Saved Players");
		plugin.debug("Saving worlds");
		for (World w : Bukkit.getWorlds())
		{
			plugin.debug(String.format("Saving world: %s", w.getName()));
			w.save();
		}
		plugin.debug("Saved Worlds");
		plugin.broadcast(configmsg.messageSaveBroadcastPost, config.saveBroadcast);
		plugin.saveInProgress = false;
	}
	
}
