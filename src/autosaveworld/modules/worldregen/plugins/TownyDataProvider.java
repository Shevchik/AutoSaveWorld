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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;

import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.bukkit.BukkitUtil;

import autosaveworld.core.GlobalConstants;
import autosaveworld.modules.worldregen.SchematicData.SchematicToLoad;
import autosaveworld.modules.worldregen.SchematicData.SchematicToSave;
import autosaveworld.modules.worldregen.tasks.CopyDataProvider;
import autosaveworld.modules.worldregen.tasks.PasteDataProvider;

public class TownyDataProvider implements CopyDataProvider, PasteDataProvider {

	private World wtoregen;

	public TownyDataProvider(World wtoregen) {
		this.wtoregen = wtoregen;
	}

	@Override
	public List<SchematicToSave> getSchematicsToCopy() throws NotRegisteredException {
		ArrayList<SchematicToSave> schematics = new ArrayList<SchematicToSave>();
		List<Town> towns = TownyUniverse.getDataSource().getWorld(wtoregen.getName()).getTowns();
		for (Town town : towns) {
			List<TownBlock> tblocks = town.getTownBlocks();
			if (tblocks.size() > 0) {
				String name = town.getName();
				for (TownBlock tb : tblocks) {
					if (tb.getWorld().getName().equalsIgnoreCase(wtoregen.getName())) {
						// get coords
						final int xcoord = tb.getX();
						final int zcoord = tb.getZ();
						final Vector bvmin = BukkitUtil.toVector(new Location(wtoregen, xcoord * 16, 0, zcoord * 16));
						final Vector bvmax = BukkitUtil.toVector(new Location(wtoregen, (xcoord * 16) + 15, wtoregen.getMaxHeight(), (zcoord * 16) + 15));
						// add to save list
						schematics.add(new SchematicToSave(
							GlobalConstants.getTownyTempFolder() + town.getName() + File.separator + "X" + xcoord + "Z" + zcoord,
							bvmin, bvmax,
							"Saving towny town "+name+" chunk to schematic",
							"Towny town "+name+" chunk saved"
						));
					}
				}
			}
		}
		return schematics;
	}

	@Override
	public List<SchematicToLoad> getSchematicsToPaste() throws NotRegisteredException {
		ArrayList<SchematicToLoad> schematics = new ArrayList<SchematicToLoad>();
		List<Town> towns = TownyUniverse.getDataSource().getWorld(wtoregen.getName()).getTowns();
		for (Town town : towns) {
			List<TownBlock> tblocks = town.getTownBlocks();
			if (tblocks.size() > 0) {
				String name = town.getName();
				for (TownBlock tb : tblocks) {
					if (tb.getWorld().getName().equalsIgnoreCase(wtoregen.getName())) {
						// get coords
						final int xcoord = tb.getX();
						final int zcoord = tb.getZ();
						// add to save list
						schematics.add(new SchematicToLoad(
							GlobalConstants.getTownyTempFolder() + town.getName() + File.separator + "X" + xcoord + "Z" + zcoord,
							"Pasting towny town "+name+" chunk from schematic",
							"Towny town "+name+" chunk pasted"
						));
					}
				}
			}
		}
		return schematics;
	}

}
