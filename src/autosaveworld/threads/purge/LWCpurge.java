package autosaveworld.threads.purge;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;

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
					if (!isActive(pr.getOwner(),awaytime))
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
