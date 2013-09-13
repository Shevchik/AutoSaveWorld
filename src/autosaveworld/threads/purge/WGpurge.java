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

package autosaveworld.threads.purge;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.World;
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
	
	public WGpurge(AutoSaveWorld plugin)
	{
		this.plugin = plugin;
	}
	
	public void doWGPurgeTask(PlayerActiveCheck pacheck, final boolean regenrg, boolean noregenoverlap) {

		WorldGuardPlugin wg = (WorldGuardPlugin) plugin.getServer()
				.getPluginManager().getPlugin("WorldGuard");
		
		plugin.debug("WG purge started");
		
		int deletedrg = 0;		
		for (final World w : Bukkit.getWorlds()) 
		{
			plugin.debug("Checking WG protections in world " + w.getName());
			final RegionManager m = wg.getRegionManager(w);
			
			// searching for inactive players in regions
			Collection<ProtectedRegion> regions = new HashSet<ProtectedRegion>(m.getRegions().values());
			for (final ProtectedRegion rg : regions) 
			{
				
				plugin.debug("Checking region " + rg.getId());
				Set<String> ddpl = rg.getOwners().getPlayers();
				int inactiveplayers = 0;
				for (String checkPlayer : ddpl) 
				{
					if (!pacheck.isActiveNCS(checkPlayer)) 
					{
						plugin.debug(checkPlayer+ " is inactive");
						inactiveplayers++;
					}
				}
				// check region for remove (ignore regions without owners)
				if (!ddpl.isEmpty() && inactiveplayers == ddpl.size()) 
				{
					plugin.debug("No active owners for region "+rg.getId()+". Purging region");
					boolean overlap = false;
					if (noregenoverlap && m.getApplicableRegions(rg).size() > 0) 
					{
						overlap = true;
					}
					final boolean rgoverlap = overlap;
					//regen should be done in main thread
					Runnable rgregen =  new Runnable()
					{
						BlockVector minpoint = rg.getMinimumPoint();
						BlockVector maxpoint = rg.getMaximumPoint();
						BukkitWorld lw = new BukkitWorld(w);
						public void run()
						{
							try {
								if (regenrg && !rgoverlap) 
								{
									plugin.debug("Regenerating region " + rg.getId());
									lw.regenerate(
											new CuboidRegion(lw,minpoint,maxpoint),
											new EditSession(lw,Integer.MAX_VALUE)
											);
								}
								plugin.debug("Deleting region " + rg.getId());
								m.removeRegion(rg.getId());
								m.save();
							} catch (Exception e) {}
						}
					};
					int taskid = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, rgregen);

			
					//Wait until previous region regeneration is finished to avoid full main thread freezing
					while (Bukkit.getScheduler().isCurrentlyRunning(taskid) || Bukkit.getScheduler().isQueued(taskid))
					{
						try {Thread.sleep(100);} catch (InterruptedException e) {}
					}
					
					deletedrg += 1;
				}
			}
		}

		plugin.debug("WG purge finished, deleted "+ deletedrg +" inactive regions");
	}

}
