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
package autosave;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.ConsoleCommandSender;

import com.griefcraft.lwc.LWCPlugin;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class AutoPurgeThread extends Thread {
	
	protected final Logger log = Logger.getLogger("Minecraft");
	private AutoSave plugin = null;
	private AutoSaveConfig config;
	private AutoSaveConfigMSG configmsg;
	private boolean run = true;
	private boolean command = false;
	AutoPurgeThread(AutoSave plugin, AutoSaveConfig config, AutoSaveConfigMSG configmsg) {
		this.plugin = plugin;
		this.config = config;
		this.configmsg = configmsg;
	}

	// Allows for the thread to naturally exit if value is false
	public void setRun(boolean run) {
		this.run = run;
	}
	private int runnow;
	public void startpurge()
	{
	command = true;
	runnow = config.purgeInterval;
	}
	
	
	// The code to run...weee
	public void run() {
		if (config == null) {
			return;
		}

		log.info(String
				.format("[%s] AutoPurgeThread Started: Interval is %d seconds, Warn Times are %s",
						plugin.getDescription().getName(), config.purgeInterval,
						Generic.join(",", config.varWarnTimes)));
		Thread.currentThread().setName("AutoSaveWorld_AutoPurgeThread");
		while (run) {
			// Prevent AutoPurge from never sleeping
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
			for (runnow = 0; runnow < config.backupInterval; runnow++) {
				try {
					if (!run) {
						if (config.varDebug) {
							log.info(String.format("[%s] Graceful quit of AutoPurgeThread", plugin.getDescription().getName()));
						}
						return;
					}
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					log.info("Could not sleep!");
				}
			}
			
			 if (config.purgeEnabled||command) performPurge();


		}
	}
	
	public void performPurge() {
		if (plugin.purgeInProgress) {
			plugin.warn("Multiple concurrent purges attempted! Purge interval is likely too short!");
			return;
			} else if (plugin.saveInProgress) {
			plugin.warn("AutoSave is in progress. Purge cancelled.");
			return;
			} else if (plugin.backupInProgress) {
			plugin.warn("AutoBackup is in progress. Purge cancelled.");	
			return;
			}	else {
		if (config.slowpurge) {setPriority(Thread.MIN_PRIORITY);}
		plugin.purgeInProgress = true;
		plugin.broadcastc(configmsg.messagePurgePre);
		long awaytime = config.purgeAwayTime*1000;
		plugin.debug("Purge started");
		if ((plugin.getServer().getPluginManager().getPlugin("WorldGuard") != null) && config.wg){
		plugin.debug("WE found, purging");
		try {
		WGpurge(awaytime);} catch (Exception e) {
			e.printStackTrace();
		}}
		if ((plugin.getServer().getPluginManager().getPlugin("LWC") != null) && config.lwc){
		plugin.debug("LWC found, purging");
		try {
			LWCpurge(awaytime);} catch (Exception e) {
				e.printStackTrace();
			}}
		plugin.debug("Purging player dat files");
		if (config.dat) {
		try {
		DelPlayerDatFile(awaytime);} catch (Exception e) {
			e.printStackTrace();
		} }
		command = false;
		plugin.debug("Purge finished");
		plugin.broadcastc(configmsg.messagePurgePost);
		plugin.purgeInProgress = false;
		if (config.slowpurge) {setPriority(Thread.NORM_PRIORITY);}
			}
	}
	
	public void WGpurge(long awaytime) {
		//don't know if all of this is thread safe, so creating values for everyfing before iterating.
		WorldGuardPlugin wg = (WorldGuardPlugin) plugin.getServer().getPluginManager().getPlugin("WorldGuard");
		List<World> worldlist = Bukkit.getWorlds();
		for(World w : worldlist) {
		plugin.debug("Checking WG protections in world "+w.getName());
		RegionManager m = wg.getRegionManager(w);
		List<String> rgtodel = new ArrayList<String>();
		for(ProtectedRegion rg
				: m.getRegions().values()) {
			plugin.debug("Checking region "+rg.getId());
			DefaultDomain dd = rg.getOwners();
			ArrayList<String> pltodelete = new ArrayList<String>();
			Set<String> ddpl = dd.getPlayers();
			for (String checkPlayer : ddpl) {
				plugin.debug("Checking player "+checkPlayer);
				if (Bukkit.getOfflinePlayer(checkPlayer).hasPlayedBefore()) {
				long timelp = Bukkit.getOfflinePlayer(checkPlayer).getLastPlayed();
				if (System.currentTimeMillis() - timelp >= awaytime)
				{
					pltodelete.add(checkPlayer);
					plugin.debug(checkPlayer+" is inactive");
				}
				} else {pltodelete.add(checkPlayer);
					plugin.debug(checkPlayer+" is inactive"); }
			}
			if (ddpl.size() <= pltodelete.size()) {
				rgtodel.add(rg.getId());
				plugin.debug("No active owners for region " +rg.getId() + " Added to removal list");
			} else {
				if (pltodelete.size() != 0) {
					for (String plrem : pltodelete) {
					dd.removePlayer(plrem);
					plugin.debug("There is still some active owners in region " +rg.getId() + " Removing inactive owners");
					}
					rg.setOwners(dd);
				}
				
			}
				try {m.save();} catch (Exception e) {}	
			
			}
			plugin.debug("Deleting region in removal list");
			for (String delrg : rgtodel)
			{
				plugin.debug("Removing region "+delrg);
				m.removeRegion(delrg);
				if (config.wgregenrg) {
					plugin.debug("Regenerating region"+delrg);
					LocalWorld lw = new BukkitWorld(w);
					lw.regenerate(new CuboidRegion(lw, m.getRegion(delrg).getMinimumPoint(), m.getRegion(delrg).getMaximumPoint()), new EditSession(lw, Integer.MAX_VALUE));
				}
			}
			try {m.save();} catch (Exception e) {}
		}
	}
	
	public void LWCpurge(long awaytime) {
		LWCPlugin lwc = (LWCPlugin) Bukkit.getPluginManager().getPlugin("LWC");
		ConsoleCommandSender sender = Bukkit.getConsoleSender();
		OfflinePlayer[] checkPlayers = Bukkit.getServer().getOfflinePlayers();
		for (OfflinePlayer pl : checkPlayers)
		{
			if (System.currentTimeMillis() - pl.getLastPlayed() >= awaytime) {
				if (config.lwcdelprotectedblocks) {
				plugin.debug(pl.getName()+" is inactive Removing all LWC protections and deleting blocks");
				lwc.getLWC().fastRemoveProtectionsByPlayer(sender, pl.getName(), true);
				}
				else {
				plugin.debug(pl.getName()+" is inactive Removing all LWC protections");
				lwc.getLWC().fastRemoveProtectionsByPlayer(sender, pl.getName(), false);	
				}
		}
		}

	}
	
	public void DelPlayerDatFile(long awaytime) {
			OfflinePlayer[] checkPlayers = Bukkit.getServer().getOfflinePlayers();
			for (OfflinePlayer pl : checkPlayers) {
				if (System.currentTimeMillis() - pl.getLastPlayed() >= awaytime) {
					//For thread safety(i don't want to know what will happen if player will join the server while his dat file is deleting from another thread)
					//The problem is how plugins will react to this, need someone to test this.
					
					//Check if the player was already banned
					boolean banned = pl.isBanned();
					if (!banned) {
					pl.setBanned(true);}
					try {
						File pldatFile= new File(new File(".").getCanonicalPath()+File.separator+Bukkit.getWorlds().get(0).getName()
								+File.separator+"players"+File.separator+pl.getName()+".dat");
						if (pldatFile.exists()) {pldatFile.delete(); plugin.debug(pl.getName()+" is inactive. Removing dat file");}
					} catch (IOException e) {e.printStackTrace();}
					//Unban after purge
					if (!banned) {
					pl.setBanned(false);}
			}
		}		
	}
	
}
