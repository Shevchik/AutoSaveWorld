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

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.schematic.SchematicFormat;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import autosaveworld.core.AutoSaveWorld;
import autosaveworld.threads.worldregen.WorldRegenConstants;

public class WorldGuardPaste {

	private AutoSaveWorld plugin;
	private World wtopaste;
	
	public WorldGuardPaste(AutoSaveWorld plugin, String worldtopasteto)
	{
		this.plugin = plugin;
		this.wtopaste = Bukkit.getWorld(worldtopasteto);
	}
	
	
	private int taskid;
	final SchematicFormat format = SchematicFormat.getFormats().iterator().next();
	final String schemfolder = WorldRegenConstants.getWGTempFolder();
	
	public void pasteAllFromSchematics()
	{
		plugin.debug("Pasting WG regions from schematics");
		
		WorldGuardPlugin wg = (WorldGuardPlugin) plugin.getServer().getPluginManager().getPlugin("WorldGuard");
		final RegionManager m = wg.getRegionManager(wtopaste);

		for (final ProtectedRegion rg : m.getRegions().values()) {
			if (rg.getId().equalsIgnoreCase("__global__")) {continue;}
			pasteWGRegion(rg);
		}
	}
	
	
	private void pasteWGRegion(final ProtectedRegion rg)
	{
		Runnable copypaste = new Runnable() 
		{
			public void run() 
			{
				try {
					plugin.debug("Pasting WG region "+rg.getId()+" from schematic");
					//load from schematic to clipboard
					EditSession es = new EditSession(new BukkitWorld(wtopaste),Integer.MAX_VALUE);
					File f = new File(schemfolder+rg.getId());
					CuboidClipboard cc = format.load(f);
					//paste clipboard at origin
					cc.place(es, cc.getOrigin(), false);
					plugin.debug("Pasted WG region "+rg.getId()+" from schematic");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		taskid = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, copypaste);
		while (Bukkit.getScheduler().isCurrentlyRunning(taskid) || Bukkit.getScheduler().isQueued(taskid)) {
			try {Thread.sleep(100);} catch (InterruptedException e){e.printStackTrace();}
		}
	}
	
}
