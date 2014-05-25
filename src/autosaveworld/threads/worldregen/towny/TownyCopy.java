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

package autosaveworld.threads.worldregen.towny;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import autosaveworld.core.GlobalConstants;
import autosaveworld.core.logging.MessageLogger;
import autosaveworld.threads.worldregen.SchematicData.SchematicToSave;
import autosaveworld.threads.worldregen.SchematicOperations;

import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.bukkit.BukkitUtil;

public class TownyCopy {

	private World wtoregen;
	public TownyCopy(String worldtoregen) {
		this.wtoregen = Bukkit.getWorld(worldtoregen);
	}

	public void copyAllToSchematics() {
		try {
			MessageLogger.debug("Saving Towny towns to schematics");

			new File(GlobalConstants.getTownyTempFolder()).mkdirs();

			List<Town> towns = TownyUniverse.getDataSource().getWorld(wtoregen.getName()).getTowns();
			for (Town town : towns) {
				List<TownBlock> tblocks = town.getTownBlocks();
				if (tblocks.size() > 0) {
					MessageLogger.debug("Saving town claim "+town.getName()+" to schematic");
					//create temp folder for town
					new File(GlobalConstants.getTownyTempFolder()+town.getName()).mkdirs();
					//save all chunks
					LinkedList<SchematicToSave> schematics = new LinkedList<SchematicToSave>();
					for (TownBlock tb : tblocks) {
						if (tb.getWorld().getName().equals(wtoregen.getName())) {
							//get coords
							final int xcoord = tb.getX();
							final int zcoord = tb.getZ();
							final Vector bvmin = BukkitUtil.toVector(
								new Location(
									wtoregen,
									xcoord*16,
									0,
									zcoord*16
								)
							);
							final Vector bvmax = BukkitUtil.toVector(
								new Location(
									wtoregen,
									xcoord*16+15,
									wtoregen.getMaxHeight(),
									zcoord*16+15
								)
							);
							//add to save list
							SchematicToSave schematicdata = new SchematicToSave(GlobalConstants.getTownyTempFolder()+town.getName()+File.separator+"X"+xcoord+"Z"+zcoord, bvmin, bvmax);
							schematics.add(schematicdata);
						}
					}
					SchematicOperations.saveToSchematic(wtoregen, schematics);
					MessageLogger.debug("Towny claim "+town.getName()+" saved");
				}
			}
		} catch (NotRegisteredException e) {
			e.printStackTrace();
		}
	}

}
