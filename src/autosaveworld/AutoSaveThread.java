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

package autosaveworld;

import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.World;

public class AutoSaveThread extends Thread {

	protected final Logger log = Bukkit.getLogger();
	private boolean run = true;
	private AutoSaveWorld plugin = null;
	private AutoSaveConfig config;
	private AutoSaveConfigMSG configmsg;
	public boolean command = false;
	// Constructor to define number of seconds to sleep
	AutoSaveThread(AutoSaveWorld plugin, AutoSaveConfig config, AutoSaveConfigMSG configmsg) {
		this.plugin = plugin;
		this.config = config;
		this.configmsg = configmsg;
	}

	// Allows for the thread to naturally exit if value is false
	public void setRun(boolean run) {
		this.run = run;
	}
	private int i;
	public void startsave()
	{
	command = true;
	i = config.saveInterval;
	}
	// The code to run...weee
	public void run() {
		if (config == null) {
			return;
		}

		log.info(String.format("[%s] AutoSaveThread Started: Interval is %d seconds",
						plugin.getDescription().getName(), config.saveInterval
					)
				);
		Thread.currentThread().setName("AutoSaveWorld_AutoSaveThread");
		while (run) {
			// Prevent AutoSave from never sleeping
			// If interval is 0, sleep for 5 seconds and skip saving
			if(config.saveInterval == 0) {
				try {
					Thread.sleep(5000);
				} catch(InterruptedException e) {
					// care
				}
				continue;
			}
			
			
			// Do our Sleep stuff!
			for (i = 0; i < config.saveInterval; i++) {
				try {
					if (!run) {
						if (config.varDebug) {

						}
						return;
					}
					boolean warn = config.savewarn;
					for (int w : config.saveWarnTimes) {
						if (w != 0 && w + i == config.saveInterval) {
						} else {warn = false;}
					}

					if (warn) {
						// Perform warning
						if (config.saveEnabled) {
							plugin.getServer().broadcastMessage(Generic.parseColor(configmsg.messageWarning));
							log.info(String.format("[%s] %s", plugin.getDescription().getName(), configmsg.messageWarning));
						}
					}
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					log.info("Could not sleep!");
				}
			}

			if (config.saveEnabled||command) {
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() { 
					public void run() {performSave();}});
			}
		}
		
		//finished
		log.info("[AutoSaveWorld] Graceful quit of AutoSaveThread");
	}
	private void savePlayers() {
		// Save the players
		plugin.debug("Saving players");
		plugin.getServer().savePlayers();
		}

	private int saveWorlds() {
		// Save our worlds
		int i = 0;
		List<World> worlds = plugin.getServer().getWorlds();
		for (World world : worlds) {
		plugin.debug(String.format("Saving world: %s", world.getName()));
		world.save();
		i++;
		}
		return i;
		}


	public void performSave() {
		if (plugin.saveInProgress) {
		plugin.warn("Multiple concurrent saves attempted! Save interval is likely too short!");
		return;
		}
		
		try {
		if (plugin.getServer().getOnlinePlayers().length == 0&&(!command)) {
		// No players online, don't bother saving.
		plugin.debug("Skipping save, no players online.");
		return;

		}

		// Lock
		plugin.saveInProgress = true;

		if (config.saveBroadcast) {plugin.broadcast(configmsg.messageBroadcastPre);}

		// Save the players
		savePlayers();
		plugin.debug("Saved Players");

		// Save the worlds
		int saved = 0;
		saved += saveWorlds();

		plugin.debug(String.format("Saved %d Worlds", saved));

		if (config.saveBroadcast) {plugin.broadcast(configmsg.messageBroadcastPost);}
		} catch (Exception e) 
		{
		if (config.saveBroadcast){plugin.broadcast("&4AutoSave Failed");}
		if (config.varDebug) {e.printStackTrace();}}
		command = false;
		plugin.LastSave =new java.text.SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(java.util.Calendar.getInstance ().getTime());
		// Release
		plugin.saveInProgress = false;
		}


}
