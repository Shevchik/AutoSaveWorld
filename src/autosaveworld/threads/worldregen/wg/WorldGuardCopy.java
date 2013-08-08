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

package autosaveworld.threads.worldregen.wg;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.World;

import autosaveworld.core.AutoSaveWorld;
import autosaveworld.threads.worldregen.WorldRegenConstants;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.schematic.SchematicFormat;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class WorldGuardCopy {

	private AutoSaveWorld plugin;
	private World wtoregen;
	
	public WorldGuardCopy(AutoSaveWorld plugin, String worldtoregen)
	{
		this.plugin = plugin;
		this.wtoregen = Bukkit.getWorld(worldtoregen);
	}
	
	
	private int taskid;
    final SchematicFormat format = SchematicFormat.getFormats().iterator().next();
	final String schemfolder = WorldRegenConstants.getWGTempFolder();
	
	public void copyAllToSchematics()
	{
		plugin.debug("Saving wg regions to schematics");
		
		WorldGuardPlugin wg = (WorldGuardPlugin) plugin.getServer().getPluginManager().getPlugin("WorldGuard");
		final RegionManager m = wg.getRegionManager(wtoregen);
		new File(schemfolder).mkdirs();

		for (final ProtectedRegion rg : m.getRegions().values()) {
			if (rg.getId().equalsIgnoreCase("__global__")) {continue;}
			saveWGRegion(rg);
		}
	}
	
	
	private void saveWGRegion(final ProtectedRegion rg)
	{
		Runnable copypaste = new Runnable() 
		{
			public void run()
			{
				try {
				plugin.debug("Saving WG Region "+rg.getId()+" to schematic");
				//copy to clipboard
				EditSession es = new EditSession(new BukkitWorld(wtoregen),Integer.MAX_VALUE);
				Vector bvmin = rg.getMinimumPoint().toBlockPoint();
				Vector bvmax = rg.getMaximumPoint().toBlockPoint();
				Vector pos = bvmax;
				CuboidClipboard clipboard = new CuboidClipboard(
						bvmax.subtract(bvmin).add(new Vector(1, 1, 1)),
						bvmin, bvmin.subtract(pos)
				);
				clipboard.copy(es);
				//save to schematic
		        File schematic = new File(schemfolder + rg.getId());
		        format.save(clipboard, schematic);
		        plugin.debug("WG Region "+rg.getId()+" saved");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		taskid = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, copypaste);
		while (Bukkit.getScheduler().isCurrentlyRunning(taskid) || Bukkit.getScheduler().isQueued(taskid))
		{
			try {Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace();}
		}
	}
}