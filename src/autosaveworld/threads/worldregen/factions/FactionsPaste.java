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
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.World;

import autosaveworld.core.GlobalConstants;
import autosaveworld.core.logging.MessageLogger;
import autosaveworld.threads.worldregen.WorldRegenPasteThread;

import com.massivecraft.factions.entity.BoardColls;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColls;
import com.massivecraft.mcore.ps.PS;

public class FactionsPaste {

	private WorldRegenPasteThread wrthread;
	private World wtopaste;
	public FactionsPaste(WorldRegenPasteThread wrthread, String worldtopasteto) {
		this.wrthread = wrthread;
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
				//paste all chunks
				for (PS ps: chunks) {
					if (ps.getWorld().equalsIgnoreCase(wtopaste.getName())) {
						final int xcoord = ps.getChunkX();
						final int zcoord = ps.getChunkZ();
						//paste
						MessageLogger.debug("Pasting "+f.getName()+" chunk from schematic");
						wrthread.getSchematicOperations().pasteFromSchematic(schemfolder+f.getName()+File.separator+"X"+xcoord+"Z"+zcoord, wtopaste);
						MessageLogger.debug("Pasted "+f.getName()+" chunk from schematic");
					}
				}
				MessageLogger.debug("Pasted faction land "+f.getName()+" from schematic");
			}
		}
	}

}
