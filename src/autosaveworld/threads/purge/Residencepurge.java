package autosaveworld.threads.purge;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

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
	
	public void doResidencePurgeTask(long awaytime, final boolean regenres)
	{
		plugin.debug("Residence purge started");
		
		int deletedres = 0;
		
		ArrayList<String> restodel = new ArrayList<String>();
		boolean wepresent = (Bukkit.getPluginManager().getPlugin("WorldEdit") != null);
		
		//search for residences with inactive players
		for (String res : Residence.getResidenceManager().getResidenceList())
		{
			plugin.debug("Checking residence " + res);
			ClaimedResidence cres = Residence.getResidenceManager().getByName(res);
			String owner = null;
			try {
				owner = cres.getOwner();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (owner != null && !isActive(owner, awaytime)) {
				plugin.debug("Owner of residence "+res+" is inactive Added to removal list");
				restodel.add(res);
			}
		}
		
		//now deal with residences that must be deleted
		for (final String res : restodel)
		{
			try 
			{
				final ClaimedResidence cres = Residence.getResidenceManager().getByName(res);
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
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
		plugin.debug("Residence purge finished, deleted "+ deletedres+" inactive residences");
		
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
