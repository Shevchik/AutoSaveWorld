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

	public PlotMepurge(AutoSaveWorld plugin, long awaytime, boolean regenplot)
	{
		this.plugin = plugin;
		PlotMePurgeTask(awaytime, regenplot);
	}
	
	
	private void PlotMePurgeTask(long awaytime, final boolean regenplot)
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
