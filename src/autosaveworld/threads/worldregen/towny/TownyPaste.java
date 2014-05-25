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
import org.bukkit.World;

import autosaveworld.core.GlobalConstants;
import autosaveworld.core.logging.MessageLogger;
import autosaveworld.threads.worldregen.SchematicData.SchematicToLoad;
import autosaveworld.threads.worldregen.SchematicOperations;

import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownyUniverse;

public class TownyPaste {

	private World wtopaste;
	public TownyPaste(String worldtopasteto) {
		this.wtopaste = Bukkit.getWorld(worldtopasteto);
	}

	public void pasteAllFromSchematics() {
		try {
			MessageLogger.debug("Pasting Towny towns from schematics");

			String schemfolder = GlobalConstants.getTownyTempFolder();

			List<Town> towns = TownyUniverse.getDataSource().getWorld(wtopaste.getName()).getTowns();
			for (Town town : towns) {
				List<TownBlock> tblocks = town.getTownBlocks();
				if (tblocks.size() > 0) {
					MessageLogger.debug("Pasting town claim "+town.getName()+" from schematic");
					LinkedList<SchematicToLoad> schematics = new LinkedList<SchematicToLoad>();
					for (TownBlock tb : tblocks) {
						if (tb.getWorld().getName().equals(wtopaste.getName())) {
							final int xcoord = tb.getX();
							final int zcoord = tb.getZ();
							SchematicToLoad schematicdata = new SchematicToLoad(schemfolder+town.getName()+File.separator+"X"+xcoord+"Z"+zcoord);
							schematics.add(schematicdata);
						}
					}
					SchematicOperations.pasteFromSchematic(wtopaste, schematics);
					MessageLogger.debug("Pasted town claim "+town.getName()+" from schematic");
				}
			}
		} catch (NotRegisteredException e) {
			e.printStackTrace();
		}
	}
}
