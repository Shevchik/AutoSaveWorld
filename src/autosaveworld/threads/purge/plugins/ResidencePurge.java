package autosaveworld.threads.purge.plugins;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.util.Vector;

import autosaveworld.core.AutoSaveWorld;
import autosaveworld.threads.purge.ActivePlayersList;
import autosaveworld.threads.purge.WorldEditRegeneration;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.CuboidArea;

public class ResidencePurge {

	private AutoSaveWorld plugin;

	public ResidencePurge(AutoSaveWorld plugin) {
		this.plugin = plugin;
	}

	public void doResidencePurgeTask(ActivePlayersList pacheck, final boolean regenres) {

		plugin.debug("Residence purge started");

		int deletedres = 0;

		List<String> reslist = new ArrayList<String>(Arrays.asList(Residence.getResidenceManager().getResidenceList()));
		boolean wepresent = (Bukkit.getPluginManager().getPlugin("WorldEdit") != null);

		//search for residences with inactive players
		for (final String res : reslist) {
			plugin.debug("Checking residence " + res);
			final ClaimedResidence cres = Residence.getResidenceManager().getByName(res);
			if (!pacheck.isActiveCS(cres.getOwner())) {
				plugin.debug("Owner of residence "+res+" is inactive. Purging residence");

				//regen residence areas if needed
				if (regenres && wepresent) {
					for (final CuboidArea ca : cres.getAreaArray()) {
						Runnable caregen =  new Runnable() {
							Vector minpoint = ca.getLowLoc().toVector();
							Vector maxpoint = ca.getHighLoc().toVector();
							@Override
							public void run() {
								plugin.debug("Regenerating residence "+res+" cuboid area");
								WorldEditRegeneration.regenerateRegion(Bukkit.getWorld(cres.getWorld()), minpoint, maxpoint);
							}
						};
						int taskid = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, caregen);

						//Wait until previous residence regeneration is finished to avoid full main thread freezing
						while (Bukkit.getScheduler().isCurrentlyRunning(taskid) || Bukkit.getScheduler().isQueued(taskid)) {
							try {Thread.sleep(100);} catch (InterruptedException e) {}
						}
					}
					//delete residence from db
					plugin.debug("Deleting residence "+res);
					Runnable delres = new Runnable() {
						@Override
						public void run() {
							cres.remove();
							Residence.getResidenceManager().save();
						}
					};
					int taskid = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, delres);

					while (Bukkit.getScheduler().isCurrentlyRunning(taskid) || Bukkit.getScheduler().isQueued(taskid)) {
						try {Thread.sleep(100);} catch (InterruptedException e) {}
					}

					deletedres += 1;
				}
			}
		}

		plugin.debug("Residence purge finished, deleted "+ deletedres+" inactive residences");
	}

}
