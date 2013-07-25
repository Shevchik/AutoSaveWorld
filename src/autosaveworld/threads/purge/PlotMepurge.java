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

import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import com.worldcretornica.plotme.Plot;
import com.worldcretornica.plotme.PlotManager;
import com.worldcretornica.plotme.SqlManager;

import autosaveworld.core.AutoSaveWorld;

public class PlotMepurge {

	private AutoSaveWorld plugin;

	public PlotMepurge(AutoSaveWorld plugin)
	{
		this.plugin = plugin;
	}
	
	
	public void doPlotMePurgeTask(long awaytime, final boolean regenplot)
	{
		int delplots = 0;
		
		for (final World w : Bukkit.getWorlds())
		{
			if (PlotManager.isPlotWorld(w))
			{
				plugin.debug("Checking plots in world "+w.getName().toLowerCase());
				
				HashSet<Plot> plotstodel = new HashSet<Plot>();
				
				HashMap<String, Plot> plotsinfo = PlotManager.getPlots(w);
				for (final Plot p : plotsinfo.values())
				{
					plugin.debug("Checking plot " + p.id);
					
					
					if (!isActive(p.getOwner(),awaytime))
					{
						plugin.debug("Plot owner is inactive. Adding plot to removal list");
						plotstodel.add(p);
					}
				}
				
					for (final Plot p : plotstodel) {
						plugin.debug("Purging plot "+p.id);
						Runnable delPlot = new Runnable()
						{
							World thisWorld = w;
							Plot PlotId = p;
							public void run()
							{
								if (regenplot)
								{
									plugin.debug("Regenerating plot "+PlotId.id);
									PlotManager.clear(thisWorld, PlotId);
								}
								plugin.debug("Deleting plot "+PlotId.id);
								PlotManager.getPlots(thisWorld).remove(PlotId.id);
								
								PlotManager.removeOwnerSign(thisWorld, PlotId.id);
								PlotManager.removeSellSign(thisWorld, PlotId.id);
													
								SqlManager.deletePlot(PlotManager.getIdX(PlotId.id), PlotManager.getIdZ(PlotId.id), thisWorld.getName().toLowerCase());
							}
						};
						int taskid = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, delPlot);
						
						
						//Wait until previous plot regeneration is finished to avoid full main thread freezing
						while (Bukkit.getScheduler().isCurrentlyRunning(taskid) || Bukkit.getScheduler().isQueued(taskid))
						{
							try {Thread.sleep(100);} catch (InterruptedException e) {}
						}
						delplots +=1;
					}
					
					
					
			}
		}
		
		plugin.debug("PlotMe purge finished, deleted "+ delplots +" inactive plots");
		
	}
	
	private boolean isActive(String player, long awaytime)
	{
		OfflinePlayer offpl = Bukkit.getOfflinePlayer(player);
		boolean active = true;
		if (System.currentTimeMillis() - offpl.getLastPlayed() >= awaytime)
		{
			active = false;
		}
		if (offpl.isOnline())
		{
			active = true;
		}
		return active;
	}
	
	
}
