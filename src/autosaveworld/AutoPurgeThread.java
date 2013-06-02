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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.griefcraft.lwc.LWCPlugin;
import com.griefcraft.model.Protection;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.api.profile.WorldGroupProfile;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class AutoPurgeThread extends Thread {

	protected final Logger log = Bukkit.getLogger();
	private AutoSaveWorld plugin = null;
	private AutoSaveConfig config;
	private AutoSaveConfigMSG configmsg;
	private boolean run = true;
	private boolean command = false;
	FileConfiguration plnopurgelistfile = null;
	HashSet<String> plnopurgelist = new HashSet<String>();

	AutoPurgeThread(AutoSaveWorld plugin, AutoSaveConfig config,
			AutoSaveConfigMSG configmsg) {
		this.plugin = plugin;
		this.config = config;
		this.configmsg = configmsg;
	}

	// Allows for the thread to naturally exit if value is false
	public void setRun(boolean run) {
		this.run = run;
	}

	private int i;

	public void startpurge() {
		command = true;
		i = config.purgeInterval;
	}

	private boolean PurgePlayer(String player) {
		if (plnopurgelist.contains(player.toLowerCase())) {
			return false;
		}
		return true;
	}

	// The code to run...weee
	public void run() {
		if (config == null) {
			return;
		}

		log.info(String.format("[%s] AutoPurgeThread Started: Interval is %d seconds",
						plugin.getDescription().getName(), config.purgeInterval
					)
				);
		Thread.currentThread().setName("AutoSaveWorld_AutoPurgeThread");

		// load list of players which will not be affected by purge
		plnopurgelistfile = YamlConfiguration.loadConfiguration(new File("plugins/AutoSaveWorld/nopurgeplayerlist.yml"));
		HashSet<String> tplnopurgelist = new HashSet<String>(plnopurgelistfile.getStringList("players"));
		for (String name : tplnopurgelist)
		{
			plnopurgelist.add(name.toLowerCase());
		}
		plnopurgelistfile.set("players", new ArrayList<String>(plnopurgelist));
		try {
			plnopurgelistfile.save(new File("plugins/AutoSaveWorld/nopurgeplayerlist.yml"));
		} catch (IOException e1) {}
		

		while (run) {
			// Prevent AutoPurge from never sleeping
			// If interval is 0, sleep for 10 seconds and skip saving
			if (config.purgeInterval == 0) {
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {}
				continue;
			}

			// Do our Sleep stuff!
			for (i = 0; i < config.purgeInterval; i++) {
				try {Thread.sleep(1000);} catch (InterruptedException e) {log.info("Could not sleep!");}
			}

			if (config.purgeEnabled || command) {performPurge();}

		}
		
		//message before disabling thread
		if (config.varDebug) {log.info("[AutoSaveWorld] Graceful quit of AutoPurgeThread");}
		
	}




	public void performPurge() {
		if (plugin.purgeInProgress) {
			plugin.warn("Multiple concurrent purges attempted! Purge interval is likely too short!");
			return;
		} else if (plugin.backupInProgress) {
			plugin.warn("AutoBackup is in progress. Purge cancelled.");
			return;
		} else {
			if (config.slowpurge) {
				setPriority(Thread.MIN_PRIORITY);
			}
			plugin.purgeInProgress = true;
			if (config.purgeBroadcast) {
				plugin.broadcast(configmsg.messagePurgePre);
			}
			
			long awaytime = config.purgeAwayTime * 1000;
			
			plugin.debug("Purge started");
			
			if ((plugin.getServer().getPluginManager().getPlugin("WorldGuard") != null)
					&& config.wg) {
				plugin.debug("WE found, purging");
				try {
					WGpurge(awaytime);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			if ((plugin.getServer().getPluginManager().getPlugin("LWC") != null)
					&& config.lwc) {
				plugin.debug("LWC found, purging");
				try {
					LWCpurge(awaytime);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			if ((Bukkit.getPluginManager().getPlugin("Multiverse-Inventories") !=null) 
					&& config.mvinv ) {
				plugin.debug("Multiverse-Inventories found, purging");
				try {
					MVInvPurge(awaytime);
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			plugin.debug("Purging player dat files");
			if (config.dat) {
				try {
					DelPlayerDatFile(awaytime);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			

			
			plugin.debug("Purge finished");
			
			if (config.purgeBroadcast) {
				plugin.broadcast(configmsg.messagePurgePost);
			}
			command = false;
			plugin.purgeInProgress = false;
			if (config.slowpurge) {
				setPriority(Thread.NORM_PRIORITY);
			}
		}
	}

	public boolean rgdelfinished = false;
	public void WGpurge(long awaytime) {

		WorldGuardPlugin wg = (WorldGuardPlugin) plugin.getServer()
				.getPluginManager().getPlugin("WorldGuard");
		
		//get all active players 
		HashSet<String> onlineplncs = new HashSet<String>();
		for (Player plname : Bukkit.getOnlinePlayers()) {
			onlineplncs.add(plname.getName().toLowerCase());
		}
		for (OfflinePlayer plname : Bukkit.getOfflinePlayers()) {
			if (System.currentTimeMillis() - plname.getLastPlayed() < awaytime) {
				onlineplncs.add(plname.getName().toLowerCase());
			}
		}
		
		
		List<World> worldlist = Bukkit.getWorlds();
		for (final World w : worldlist) {
			plugin.debug("Checking WG protections in world " + w.getName());
			final RegionManager m = wg.getRegionManager(w);
			
			// searching for inactive players in regions
			List<String> rgtodel = new ArrayList<String>();
			for (ProtectedRegion rg : m.getRegions().values()) {
				
				plugin.debug("Checking region " + rg.getId());
				ArrayList<String> pltodelete = new ArrayList<String>();
				Set<String> ddpl = rg.getOwners().getPlayers();
				for (String checkPlayer : ddpl) {
					if (PurgePlayer(checkPlayer)) {
						if (!onlineplncs.contains(checkPlayer)) {
							pltodelete.add(checkPlayer);
						}
					}
				}

				// check region for remove (ignore regions without owners)
				if (!ddpl.isEmpty()) {
					if (pltodelete.size()  == ddpl.size()) {
						// adding region to removal list, we will work with them later
						rgtodel.add(rg.getId());
						plugin.debug("No active owners for region "+rg.getId()+" Added to removal list");
					}
				}

			}

			// now deal with the regions that must be deleted
			for (final String delrg : rgtodel) {
				plugin.debug("Purging region " + delrg);

					//regen should be done in main thread
				    rgdelfinished = false;
					Runnable rgregen =  new Runnable()
					{
						BlockVector minpoint = m.getRegion(delrg).getMinimumPoint();
						BlockVector maxpoint = m.getRegion(delrg).getMaximumPoint();
						BukkitWorld lw = new BukkitWorld(w);
						public void run()
						{
							try {
								if (config.wgregenrg) {
									plugin.debug("Regenerating region" + delrg);
									new BukkitWorld(w).regenerate(
											new CuboidRegion(lw,minpoint,maxpoint),
											new EditSession(lw,Integer.MAX_VALUE)
											);
								}
								plugin.debug("Deleting region " + delrg);
								m.removeRegion(delrg);
								m.save();
							} catch (Exception e) {}
							plugin.purgeThread.rgdelfinished = true;
						}
					};
					Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, rgregen);

			
					//Wait until previous region regeneration is finished to avoid full main thread freezing
					while (!rgdelfinished)
					{
						try {Thread.sleep(100);} catch (InterruptedException e) {}
					}
									
			}		
		}
	}

	public void LWCpurge(long awaytime) {
		LWCPlugin lwc = (LWCPlugin) Bukkit.getPluginManager().getPlugin("LWC");
		//we will check LWC database and remove protections that belongs to away player
		for (final Protection pr : lwc.getLWC().getPhysicalDatabase().loadProtections())
		{
			Player pl = pr.getBukkitOwner();
			if (PurgePlayer(pl.getName()))
				{ 
					if (!pr.getBukkitOwner().hasPlayedBefore() || System.currentTimeMillis() - pl.getLastPlayed() >= awaytime)
					{
						if (config.lwcdelprotectedblocks)
						{

							Runnable remchest = new Runnable()
							{
								Block chest = pr.getBlock();
								public void run() {
									chest.setType(Material.AIR);
								}
								
							};
							Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, remchest);
						}
						plugin.debug("Removing protection for inactive player "+pr.getBukkitOwner().getName());
						lwc.getLWC().getPhysicalDatabase().removeProtection(pr.getId());
					}
				}
		}
	}


	public void MVInvPurge(long awaytime)
	{
		try {
		MultiverseInventories mvpl = (MultiverseInventories) Bukkit.getPluginManager().getPlugin("Multiverse-Inventories");
		File mcinvpfld = new File("plugins/Multiverse-Inventories/players/");
		//We will get all files from MVInv player directory, and get player names from there
		for (String plfile : mcinvpfld.list())
		{
			String plname = plfile.substring(0, plfile.indexOf("."));
			if (PurgePlayer(plname)) {
				boolean remove = false;
				OfflinePlayer offpl = (Bukkit.getOfflinePlayer(plname));
				if (!offpl.hasPlayedBefore()) {remove = true;}
				else if (System.currentTimeMillis() - offpl.getLastPlayed() >= awaytime) {remove = true;}
				if (remove) {
					plugin.debug("Removing "+plname+" MVInv files");
					//remove files from MVInv world folders
					for (World wname : Bukkit.getWorlds()) {
						mvpl.getWorldManager().getWorldProfile(wname.getName()).removeAllPlayerData(offpl);
					}
					//remove files from MVInv player folder
					new File(mcinvpfld,plfile).delete();
					//remove files from MVInv groups folder
					for (WorldGroupProfile gname: mvpl.getGroupManager().getGroups())
					{
						File mcinvgfld = new File("plugins/Multiverse-Inventories/groups/");
						new File(mcinvgfld,gname.getName()+File.separator+plfile).delete();
					}
				}
			}
		}
		} catch (Exception e) {e.printStackTrace();}
	}
	
	public void DelPlayerDatFile(long awaytime) {
		OfflinePlayer[] checkPlayers = Bukkit.getServer().getOfflinePlayers();
		for (OfflinePlayer pl : checkPlayers) {
			if (PurgePlayer(pl.getName())) {
				if (System.currentTimeMillis() - pl.getLastPlayed() >= awaytime) {

					try {
					String worldfoldername = Bukkit.getWorlds().get(0).getWorldFolder().getCanonicalPath();
							File pldatFile = new File(
											worldfoldername
											+ File.separator + "players"
											+ File.separator + pl.getName()
											+ ".dat");
							pldatFile.delete();
							plugin.debug(pl.getName()
									+ " is inactive. Removing dat file");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

}
