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

package autosaveworld.threads.worldregen.factions;

import java.io.File;
import java.util.LinkedList;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.World;

import autosaveworld.core.GlobalConstants;
import autosaveworld.core.logging.MessageLogger;
import autosaveworld.threads.worldregen.SchematicData.SchematicToLoad;
import autosaveworld.threads.worldregen.SchematicOperations;

import com.massivecraft.factions.entity.BoardColls;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColls;
import com.massivecraft.mcore.ps.PS;

public class FactionsPaste {

	private World wtopaste;
	public FactionsPaste(String worldtopasteto) {
		this.wtopaste = Bukkit.getWorld(worldtopasteto);
	}

	public void pasteAllFromSchematics() {
		MessageLogger.debug("Pasting factions lands from schematics");

		String schemfolder = GlobalConstants.getFactionsTempFolder();

		for (final Faction f : FactionColls.get().getForWorld(wtopaste.getName()).getAll()) {
			Set<PS> chunks = BoardColls.get().getChunks(f);
			//ignore factions with no claimed land
			if (chunks.size() != 0) {
				MessageLogger.debug("Pasting faction land "+f.getName()+" from schematic");
				LinkedList<SchematicToLoad> schematics = new LinkedList<SchematicToLoad>();
				//paste all chunks
				for (PS ps: chunks) {
					if (ps.getWorld().equalsIgnoreCase(wtopaste.getName())) {
						final int xcoord = ps.getChunkX();
						final int zcoord = ps.getChunkZ();
						SchematicToLoad schematicdata = new SchematicToLoad(schemfolder+f.getName()+File.separator+"X"+xcoord+"Z"+zcoord, wtopaste);
						schematics.add(schematicdata);
					}
				}
				SchematicOperations.pasteFromSchematic(schematics);
				MessageLogger.debug("Pasted faction land "+f.getName()+" from schematic");
			}
		}
	}

}
