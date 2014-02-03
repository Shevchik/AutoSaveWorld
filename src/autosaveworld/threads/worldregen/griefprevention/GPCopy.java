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

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.bukkit.BukkitUtil;

import autosaveworld.core.AutoSaveWorld;
import autosaveworld.threads.worldregen.WorldRegenCopyThread;

public class GPCopy {

	private AutoSaveWorld plugin;
	private WorldRegenCopyThread wrthread;
	private World wtoregen;
	public GPCopy(AutoSaveWorld plugin, WorldRegenCopyThread wrthread, String worldtoregen)
	{
		this.plugin = plugin;
		this.wrthread = wrthread;
		this.wtoregen = Bukkit.getWorld(worldtoregen);
	}

	public void copyAllToSchematics()
	{
		plugin.debug("Saving griefprevention regions to schematics");

		new File(plugin.constants.getGPTempFolder()).mkdirs();

		GriefPrevention gp = (GriefPrevention) Bukkit.getPluginManager().getPlugin("GriefPrevention");

		//get database
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

		//save all claims
		for (int i = 0; i<ca.size(); i++)
		{
			Claim claim = ca.get(i);
			//get coords
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
			//save
			plugin.debug("Saving GP Region "+claim.getID()+" to schematic");
			wrthread.getSchematicOperations().saveToSchematic(plugin.constants.getGPTempFolder()+claim.getID().toString(), wtoregen, bvmin, bvmax);
			plugin.debug("GP Region "+claim.getID()+" saved");
		}
	}

}
