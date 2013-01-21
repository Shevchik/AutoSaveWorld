/**
 * 
 * Copyright 2011 MilkBowl (https://github.com/MilkBowl)
 * Copyright 2012 Shevchik
 * 
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

package autosave;

import java.util.List;
import java.util.logging.Logger;

import org.bukkit.World;

public class AutoSaveThread extends Thread {

	protected final Logger log = Logger.getLogger("Minecraft");
	private boolean run = true;
	private AutoSave plugin = null;
	private AutoSaveConfig config;
	private AutoSaveConfigMSG configmsg;
	private boolean command = false;
	// Constructor to define number of seconds to sleep
	AutoSaveThread(AutoSave plugin, AutoSaveConfig config, AutoSaveConfigMSG configmsg) {
		this.plugin = plugin;
		this.config = config;
		this.configmsg = configmsg;
	}

	// Allows for the thread to naturally exit if value is false
	public void setRun(boolean run) {
		this.run = run;
	}
	private int runnow;
	public void startsave()
	{
	command = true;
	runnow = config.varInterval;
	}
	// The code to run...weee
	public void run() {
		if (config == null) {
			return;
		}

		log.info(String
				.format("[%s] AutoSaveThread Started: Interval is %d seconds, Warn Times are %s",
						plugin.getDescription().getName(), config.varInterval,
						Generic.join(",", config.varWarnTimes)));
		while (run) {
			// Prevent AutoSave from never sleeping
			// If interval is 0, sleep for 5 seconds and skip saving
			if(config.varInterval == 0) {
				try {
					Thread.sleep(5000);
				} catch(InterruptedException e) {
					// care
				}
				continue;
			}
			
			
			// Do our Sleep stuff!
			for (runnow = 0; runnow < config.varInterval; runnow++) {
				try {
					if (!run) {
						if (config.varDebug) {
							log.info(String.format("[%s] Graceful quit of AutoSaveThread", plugin.getDescription().getName()));
						}
						return;
					}
					boolean warn = config.savewarn;
					for (int w : config.varWarnTimes) {
						if (w != 0 && w + runnow == config.varInterval) {
						} else {warn = false;}
					}

					if (warn) {
						// Perform warning
						if (config.varDebug) {
							log.info(String.format("[%s] Warning Time Reached: %d seconds to go.", plugin.getDescription().getName(), config.varInterval - runnow));
						}
						plugin.getServer().broadcastMessage(Generic.parseColor(configmsg.messageWarning));
						log.info(String.format("[%s] %s", plugin.getDescription().getName(), configmsg.messageWarning));
					}
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					log.info("Could not sleep!");
				}
			}

			plugin.getServer().getScheduler().runTask(plugin, new Runnable() { 
				public void run() {performSave();}});
		}
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


	private void performSave() {
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

		plugin.broadcasta(configmsg.messageBroadcastPre);

		// Save the players
		savePlayers();
		plugin.debug("Saved Players");

		// Save the worlds
		int saved = 0;
		saved += saveWorlds();

		plugin.debug(String.format("Saved %d Worlds", saved));

		plugin.broadcasta(configmsg.messageBroadcastPost);
		} catch (Exception e) 
		{plugin.broadcasta("&4Save Failed");
		if (config.varDebug) {e.printStackTrace();}}
		// Release
		plugin.saveInProgress = false;
		}


}
