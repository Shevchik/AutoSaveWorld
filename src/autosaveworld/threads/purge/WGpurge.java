package autosaveworld.threads.purge;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import autosaveworld.core.AutoSaveWorld;

public class WGpurge {
	
	private AutoSaveWorld plugin;
	
	public WGpurge(AutoSaveWorld plugin, long awaytime, boolean regenrg)
	{
		this.plugin = plugin;
		WGPurgeTask(awaytime, regenrg);
	}
	
	private void WGPurgeTask(long awaytime, final boolean regenrg) {

		WorldGuardPlugin wg = (WorldGuardPlugin) plugin.getServer()
				.getPluginManager().getPlugin("WorldGuard");
		
		plugin.debug("WG purge started");
		
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
		
		int deletedrg = 0;
		
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
						if (!onlineplncs.contains(checkPlayer)) {
							pltodelete.add(checkPlayer);
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
					plugin.purgeThread.wgrgregenrunning = true;
					//regen should be done in main thread
					Runnable rgregen =  new Runnable()
					{
						BlockVector minpoint = m.getRegion(delrg).getMinimumPoint();
						BlockVector maxpoint = m.getRegion(delrg).getMaximumPoint();
						BukkitWorld lw = new BukkitWorld(w);
						public void run()
						{
							try {
								if (regenrg) {
									plugin.debug("Regenerating region" + delrg);
									new BukkitWorld(w).regenerate(
											new CuboidRegion(lw,minpoint,maxpoint),
											new EditSession(lw,Integer.MAX_VALUE)
											);
								}
								plugin.debug("Deleting region " + delrg);
								m.removeRegion(delrg);
								m.save();
								plugin.purgeThread.wgrgregenrunning = false;
							} catch (Exception e) {}
						}
					};
					Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, rgregen);

			
					//Wait until previous region regeneration is finished to avoid full main thread freezing
					while (plugin.purgeThread.wgrgregenrunning)
					{
						try {Thread.sleep(100);} catch (InterruptedException e) {}
					}
									
			}	
			
			deletedrg += rgtodel.size();
			
		}
		
		plugin.debug("WG purge finished, deleted "+ deletedrg +" inactive regions");
		
	}
}
