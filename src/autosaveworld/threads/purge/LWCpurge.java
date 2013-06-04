package autosaveworld.threads.purge;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import autosaveworld.core.AutoSaveWorld;

import com.griefcraft.lwc.LWCPlugin;
import com.griefcraft.model.Protection;

public class LWCpurge {

	private AutoSaveWorld plugin;

	public LWCpurge(AutoSaveWorld plugin, long awaytime, boolean delblocks)
	{
		this.plugin = plugin;
		LWCPurgeTask(awaytime, delblocks);
	}
	
	public void LWCPurgeTask(long awaytime, boolean delblocks) {
		LWCPlugin lwc = (LWCPlugin) Bukkit.getPluginManager().getPlugin("LWC");
		
		plugin.debug("LWC purge started");
		
		int deleted = 0;
		
		
		//we will check LWC database and remove protections that belongs to away player
		for (final Protection pr : lwc.getLWC().getPhysicalDatabase().loadProtections())
		{
			boolean remove = false;
			Player pl = pr.getBukkitOwner();
			//check is the player is inactive
			if (!pl.hasPlayedBefore()) {remove = true;}
			else if (System.currentTimeMillis() - pl.getLastPlayed() >= awaytime) {remove = true;}
			//rare occasion when player just joined server, then hasPlayedBefore will return false for this player
			if (pl.isOnline()) {remove = false;}
					if (remove)
					{
						//delete block
						if (delblocks)
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
						//delete protections
						lwc.getLWC().getPhysicalDatabase().removeProtection(pr.getId());
						//count deleted protections
						deleted += 1;
					}
		}
		
		plugin.debug("LWC purge finished, deleted "+ deleted+" inactive protections");
		
	}
	
}
