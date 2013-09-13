package autosaveworld.threads.purge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;

import autosaveworld.core.AutoSaveWorld;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.CuboidArea;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldguard.bukkit.BukkitUtil;

public class Residencepurge {

	private AutoSaveWorld plugin;
	
	public Residencepurge(AutoSaveWorld plugin)
	{
		this.plugin = plugin;
	}
	
	public void doResidencePurgeTask(PlayerActiveCheck pacheck, final boolean regenres)
	{
		plugin.debug("Residence purge started");
		
		int deletedres = 0;
		
		List<String> reslist = new ArrayList<String>(Arrays.asList(Residence.getResidenceManager().getResidenceList()));
		boolean wepresent = (Bukkit.getPluginManager().getPlugin("WorldEdit") != null);
		
		//search for residences with inactive players
		for (final String res : reslist)
		{
			plugin.debug("Checking residence " + res);
			final ClaimedResidence cres = Residence.getResidenceManager().getByName(res);
			if (!pacheck.isActiveCS(cres.getOwner())) 
			{
				plugin.debug("Owner of residence "+res+" is inactive. Purgin residence");

				//regen residence areas if needed
				if (regenres && wepresent)
				{
					for (final CuboidArea ca : cres.getAreaArray())
					{
						Runnable caregen =  new Runnable()
						{
							Vector minpoint = BukkitUtil.toVector(ca.getLowLoc());
							Vector maxpoint = BukkitUtil.toVector(ca.getHighLoc());
							BukkitWorld lw = new BukkitWorld(Bukkit.getWorld(cres.getWorld()));
							public void run()
							{
								try {
									plugin.debug("Regenerating residence "+res+" cuboid area");
										lw.regenerate(
												new CuboidRegion(lw,minpoint,maxpoint),
												new EditSession(lw,Integer.MAX_VALUE)
												);
								} catch (Exception e) {}
							}
						};
						int taskid = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, caregen);
						
						//Wait until previous residence regeneration is finished to avoid full main thread freezing
						while (Bukkit.getScheduler().isCurrentlyRunning(taskid) || Bukkit.getScheduler().isQueued(taskid))
						{
							try {Thread.sleep(100);} catch (InterruptedException e) {}
						}
					}
					//delete residence from db
					plugin.debug("Deleting residence "+res);
					Runnable delres = new Runnable()
					{
						public void run()
						{
							cres.remove();
							Residence.getResidenceManager().save();
						}
					};
					int taskid = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, delres);
					
					while (Bukkit.getScheduler().isCurrentlyRunning(taskid) || Bukkit.getScheduler().isQueued(taskid))
					{
						try {Thread.sleep(100);} catch (InterruptedException e) {}
					}
					
					deletedres += 1;
				}
			}
		}

		plugin.debug("Residence purge finished, deleted "+ deletedres+" inactive residences");
	}

}
