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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import autosaveworld.core.GlobalConstants;
import autosaveworld.core.logging.MessageLogger;
import autosaveworld.threads.worldregen.SchematicData.SchematicToSave;
import autosaveworld.threads.worldregen.SchematicOperations;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.bukkit.BukkitUtil;

public class GPCopy {

	private World wtoregen;

	public GPCopy(String worldtoregen) {
		wtoregen = Bukkit.getWorld(worldtoregen);
	}

	@SuppressWarnings("unchecked")
	public void copyAllToSchematics() {
		MessageLogger.debug("Saving griefprevention regions to schematics");

		new File(GlobalConstants.getGPTempFolder()).mkdirs();

		GriefPrevention gp = (GriefPrevention) Bukkit.getPluginManager().getPlugin("GriefPrevention");

		// get database
		ArrayList<Claim> claimArray = null;
		try {
			Field fld = DataStore.class.getDeclaredField("claims");
			fld.setAccessible(true);
			Object o = fld.get(gp.dataStore);
			claimArray = (ArrayList<Claim>) o;
		} catch (Throwable e) {
			throw new RuntimeException("Can't access GriefPrevention database", e);
		}

		// save all claims
		for (int i = 0; i < claimArray.size(); i++) {
			Claim claim = claimArray.get(i);
			// get coords
			double xmin = claim.getLesserBoundaryCorner().getX();
			double zmin = claim.getLesserBoundaryCorner().getZ();
			double xmax = claim.getGreaterBoundaryCorner().getX();
			double zmax = claim.getGreaterBoundaryCorner().getZ();
			Vector bvmin = BukkitUtil.toVector(new Location(wtoregen, xmin, 0, zmin));
			Vector bvmax = BukkitUtil.toVector(new Location(wtoregen, xmax, wtoregen.getMaxHeight(), zmax));
			// save
			MessageLogger.debug("Saving GP Region " + claim.getID() + " to schematic");
			SchematicToSave schematicdata = new SchematicToSave(GlobalConstants.getGPTempFolder() + claim.getID().toString(), bvmin, bvmax);
			SchematicOperations.saveToSchematic(wtoregen, new LinkedList<SchematicToSave>(Arrays.asList(schematicdata)));
			MessageLogger.debug("GP Region " + claim.getID() + " saved");
		}
	}

}
