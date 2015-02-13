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

package autosaveworld.modules.worldregen.plugins;

import java.util.ArrayList;
import java.util.List;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.bukkit.BukkitUtil;

import autosaveworld.core.GlobalConstants;
import autosaveworld.modules.worldregen.SchematicData.SchematicToLoad;
import autosaveworld.modules.worldregen.SchematicData.SchematicToSave;
import autosaveworld.modules.worldregen.tasks.CopyDataProvider;
import autosaveworld.modules.worldregen.tasks.PasteDataProvider;

public class GriefPreventionDataProvider implements CopyDataProvider, PasteDataProvider {

	private World wtoregen;

	public GriefPreventionDataProvider(World wtoregen) {
		this.wtoregen = wtoregen;
	}

	@Override
	public List<SchematicToSave> getSchematicsToCopy() {
		ArrayList<SchematicToSave> schematics = new ArrayList<SchematicToSave>();
		GriefPrevention gp = (GriefPrevention) Bukkit.getPluginManager().getPlugin("GriefPrevention");
		for (Claim claim : gp.dataStore.getClaims()) {
			// get coords
			double xmin = claim.getLesserBoundaryCorner().getX();
			double zmin = claim.getLesserBoundaryCorner().getZ();
			double xmax = claim.getGreaterBoundaryCorner().getX();
			double zmax = claim.getGreaterBoundaryCorner().getZ();
			Vector bvmin = BukkitUtil.toVector(new Location(wtoregen, xmin, 0, zmin));
			Vector bvmax = BukkitUtil.toVector(new Location(wtoregen, xmax, wtoregen.getMaxHeight(), zmax));
			// add to save list
			String name = claim.getID().toString();
			schematics.add(new SchematicToSave(
				GlobalConstants.getGPTempFolder() + name,
				bvmin, bvmax,
				"Saving GP Region " + name + " to schematic",
				"GP Region " + name + " saved"
			));
		}
		return schematics;
	}

	@Override
	public List<SchematicToLoad> getSchematicsToPaste() {
		ArrayList<SchematicToLoad> schematics = new ArrayList<SchematicToLoad>();
		GriefPrevention gp = (GriefPrevention) Bukkit.getPluginManager().getPlugin("GriefPrevention");
		for (Claim claim : gp.dataStore.getClaims()) {
			String name = claim.getID().toString();
			schematics.add(new SchematicToLoad(
				GlobalConstants.getGPTempFolder() + name,
				"Pasting GP Region " + name + " from schematic",
				"GP Region " + name + " pasted"
			));
		}
		return schematics;
	}

}
