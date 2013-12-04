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

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;

import autosaveworld.core.AutoSaveWorld;

import com.griefcraft.lwc.LWCPlugin;
import com.griefcraft.model.Protection;

public class LWCPurge {

	private AutoSaveWorld plugin;

	public LWCPurge(AutoSaveWorld plugin)
	{
		this.plugin = plugin;
	}
	
	public void doLWCPurgeTask(ActivePlayersList pacheck, boolean delblocks) {
		LWCPlugin lwc = (LWCPlugin) Bukkit.getPluginManager().getPlugin("LWC");
		
		plugin.debug("LWC purge started");
		
		int deleted = 0;
		
		//we will check LWC database and remove protections that belongs to away player
		for (final Protection pr : lwc.getLWC().getPhysicalDatabase().loadProtections())
		{
			if (!pacheck.isActiveCS(pr.getOwner()))
			{
				//delete block
				if (delblocks)
				{
					Runnable remchest = new Runnable()
					{
						Block block = pr.getBlock();
						public void run() 
						{
							BlockState bs = block.getState();
							if (bs instanceof Chest)
							{
								((Chest) bs).getBlockInventory().clear();
							} else
							if (bs instanceof DoubleChest)
							{
								((DoubleChest) bs).getInventory().clear();
							}
							block.setType(Material.AIR);
						}				
					};
					int taskid = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, remchest);
				
					while (Bukkit.getScheduler().isCurrentlyRunning(taskid) || Bukkit.getScheduler().isQueued(taskid))
					{
						try {Thread.sleep(50);} catch (InterruptedException e) {}
					}
				}
				plugin.debug("Removing protection for inactive player "+pr.getOwner());
				//delete protections
				lwc.getLWC().getPhysicalDatabase().removeProtection(pr.getId());
				//count deleted protections
				deleted += 1;
			}
		}
		
		plugin.debug("LWC purge finished, deleted "+ deleted+" inactive protections");
	}
	
}
