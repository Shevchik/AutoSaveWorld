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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
			HashSet<ProtectedRegion> regions = new HashSet<ProtectedRegion>(m.getRegions().values());
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
					if (regenrg)
					{
						//regen and delete region
						purgeRG(m,w,rg,regenrg,noregenoverlap);
					} else
					{
						//add region to batch delete
						deleteRGbatch(m,rg);
					}
					deletedrg += 1;
				}
			}
		}

		plugin.debug("WG purge finished, deleted "+ deletedrg +" inactive regions");
	}
	
	private void purgeRG(final RegionManager m, final World w, final ProtectedRegion rg, final boolean regenrg, boolean noregenoverlap)
	{
		final boolean donotregen = noregenoverlap && m.getApplicableRegions(rg).size() > 0;
		Runnable rgregen =  new Runnable()
		{
			BlockVector minpoint = rg.getMinimumPoint();
			BlockVector maxpoint = rg.getMaximumPoint();
			BukkitWorld lw = new BukkitWorld(w);
			public void run()
			{
				try {
					if (!donotregen) 
					{
						plugin.debug("Regenerating region " + rg.getId());
						lw.regenerate(
							new CuboidRegion(lw,minpoint,maxpoint),
							new EditSession(lw,Integer.MAX_VALUE)
						);
					}
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
	}
	
	private List<String> rgtodel = new ArrayList<String>();
	private void deleteRGbatch(final RegionManager m, final ProtectedRegion rg)
	{
		if (rgtodel.size() < 40)
		{
			//add region to delete batch
			rgtodel.add(rg.getId());
		} else
		{
			//detete regions
			Runnable deleteregions = new Runnable()
			{
				public void run()
				{
					for (String regionid : rgtodel)
					{
						m.removeRegion(regionid);
					}
					try {
						m.save();
					} catch (Exception e) {}
					rgtodel.clear();
				}
			};
			int taskid = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, deleteregions);
			
			//Wait until previous regions delete is finished to avoid full main thread freezing
			while (Bukkit.getScheduler().isCurrentlyRunning(taskid) || Bukkit.getScheduler().isQueued(taskid))
			{
				try {Thread.sleep(100);} catch (InterruptedException e) {}
			}
		}
	}
	
}
