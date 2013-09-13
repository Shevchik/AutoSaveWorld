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

package autosaveworld.threads.worldregen.griefprevention;

import java.io.File;
import java.lang.reflect.Field;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimArray;
import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.schematic.SchematicFormat;
import com.sk89q.worldguard.bukkit.BukkitUtil;

import autosaveworld.core.AutoSaveWorld;

public class GPCopy {

	private AutoSaveWorld plugin;
	private World wtoregen;
	
	public GPCopy(AutoSaveWorld plugin, String worldtoregen)
	{
		this.plugin = plugin;
		this.wtoregen = Bukkit.getWorld(worldtoregen);
		schemfolder = plugin.constants.getGPTempFolder();
	}
	
	private int taskid;
    final SchematicFormat format = SchematicFormat.getFormats().iterator().next();
	final String schemfolder;
	
	public void copyAllToSchematics()
	{
		plugin.debug("Saving griefprevention regions to schematics");
		
		GriefPrevention gp = (GriefPrevention) Bukkit.getPluginManager().getPlugin("GriefPrevention"); 
		ClaimArray ca = null;
		try {
            Field fld = DataStore.class.getDeclaredField("claims");
            fld.setAccessible(true);
            Object o = fld.get(gp.dataStore);
            ca = (ClaimArray) o;
		} 
		catch (Exception e)
		{
			e.printStackTrace();
			Bukkit.getLogger().severe("[AutoSaveWorld] Failed to access GriefPrevntion database. GP save cancelled");
			return;
		}
		new File(schemfolder).mkdirs();

		for (int i = 0; i<ca.size(); i++)
		{
			Claim claim = ca.get(i);
			saveGPRegion(claim);
		}
	}
	
	
	private void saveGPRegion(final Claim claim)
	{
		Runnable copypaste = new Runnable() 
		{
			public void run()
			{
				try {
				plugin.debug("Saving GP Region "+claim.getID()+" to schematic");
				//copy to clipboard
				EditSession es = new EditSession(new BukkitWorld(wtoregen),Integer.MAX_VALUE);
				double xmin = claim.getLesserBoundaryCorner().getX();
				double zmin = claim.getLesserBoundaryCorner().getZ();
				double xmax = claim.getGreaterBoundaryCorner().getX();
				double zmax = claim.getGreaterBoundaryCorner().getZ();
				Vector bvmin = BukkitUtil.toVector(
						new Location(
								wtoregen,
								xmin,
								0,
								zmin
						)
				);
				Vector bvmax = BukkitUtil.toVector(
						new Location(
								wtoregen,
								xmax,
								wtoregen.getMaxHeight(),
								zmax
						)
				);
				Vector pos = bvmax;
				CuboidClipboard clipboard = new CuboidClipboard(
						bvmax.subtract(bvmin).add(new Vector(1, 1, 1)),
						bvmin, bvmin.subtract(pos)
				);
				clipboard.copy(es);
				//save to schematic
		        File schematic = new File(schemfolder + claim.getID());
		        format.save(clipboard, schematic);
		        plugin.debug("GP Region "+claim.getID()+" saved");
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
