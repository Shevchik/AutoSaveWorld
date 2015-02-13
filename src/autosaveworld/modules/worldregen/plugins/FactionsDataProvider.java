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
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.World;

import autosaveworld.core.GlobalConstants;
import autosaveworld.modules.worldregen.SchematicData.SchematicToLoad;
import autosaveworld.modules.worldregen.SchematicData.SchematicToSave;
import autosaveworld.modules.worldregen.tasks.CopyDataProvider;
import autosaveworld.modules.worldregen.tasks.PasteDataProvider;

import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;
import com.massivecraft.massivecore.ps.PS;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.bukkit.BukkitUtil;

public class FactionsDataProvider implements CopyDataProvider, PasteDataProvider {

	private World wtoregen;

	public FactionsDataProvider(World wtoregen) {
		this.wtoregen = wtoregen;
	}

	@Override
	public List<SchematicToSave> getSchematicsToCopy() {
		ArrayList<SchematicToSave> schematics = new ArrayList<SchematicToSave>();
		for (final Faction f : FactionColl.get().getAll()) {
			Set<PS> chunks = BoardColl.get().getChunks(f);
			// ignore factions with no claimed land
			if (chunks.size() != 0) {
				String name = f.getName();
				for (PS ps : chunks) {
					if (ps.getWorld().equalsIgnoreCase(wtoregen.getName())) {
						// get coords
						final int xcoord = ps.getChunkX();
						final int zcoord = ps.getChunkZ();
						final Vector bvmin = BukkitUtil.toVector(new Location(wtoregen, xcoord * 16, 0, zcoord * 16));
						final Vector bvmax = BukkitUtil.toVector(new Location(wtoregen, (xcoord * 16) + 15, wtoregen.getMaxHeight(), (zcoord * 16) + 15));
						// add to save list
						schematics.add(new SchematicToSave(
							GlobalConstants.getFactionsTempFolder() + name + File.separator + "X" + xcoord + "Z" + zcoord,
							bvmin, bvmax,
							"Saving faction land "+name+" chunk to schematic",
							"Faction land "+name+" chunk saved"
						));
					}
				}
			}
		}
		return schematics;
	}

	@Override
	public List<SchematicToLoad> getSchematicsToPaste() {
		ArrayList<SchematicToLoad> schematics = new ArrayList<SchematicToLoad>();
		for (final Faction f : FactionColl.get().getAll()) {
			Set<PS> chunks = BoardColl.get().getChunks(f);
			// ignore factions with no claimed land
			if (chunks.size() != 0) {
				String name = f.getName();
				for (PS ps : chunks) {
					if (ps.getWorld().equalsIgnoreCase(wtoregen.getName())) {
						final int xcoord = ps.getChunkX();
						final int zcoord = ps.getChunkZ();
						schematics.add(new SchematicToLoad(
							GlobalConstants.getFactionsTempFolder() + name + File.separator + "X" + xcoord + "Z" + zcoord,
							"Pasting faction land "+name+" chunk from schematic",
							"Faction land "+name+" chunk pasted"
						));
					}
				}
			}
		}
		return schematics;
	}

}
