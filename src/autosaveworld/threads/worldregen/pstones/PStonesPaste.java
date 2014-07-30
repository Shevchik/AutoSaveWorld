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

package autosaveworld.threads.worldregen.pstones;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;

import org.bukkit.Bukkit;
import org.bukkit.World;

import autosaveworld.core.GlobalConstants;
import autosaveworld.core.logging.MessageLogger;
import autosaveworld.threads.worldregen.SchematicOperations;
import autosaveworld.threads.worldregen.SchematicData.SchematicToLoad;

public class PStonesPaste {

	private World wtopaste;
	public PStonesPaste(String worldtopasteto) {
		this.wtopaste = Bukkit.getWorld(worldtopasteto);
	}


	public void pasteAllFromSchematics() {
		MessageLogger.debug("Pasting PreciousStones regions from schematics");

		PreciousStones pstones = PreciousStones.getInstance();

		HashSet<Field> fields = new HashSet<Field>();
		for (Field field : pstones.getForceFieldManager().getFields("*", wtopaste)) {
			if (field.isParent()) {
				fields.add(field);
			}
		}

		String schemfolder = GlobalConstants.getPStonesTempFolder();
		for (Field field : fields) {
			MessageLogger.debug("Pasting PreciousStones region "+field.getId()+" from schematic");
			SchematicToLoad schematicdata = new SchematicToLoad(schemfolder+field.getId());
			SchematicOperations.pasteFromSchematic(wtopaste, new LinkedList<SchematicToLoad>(Arrays.asList(schematicdata)));
			MessageLogger.debug("Pasted PreciousStones region "+field.getId()+" from schematic");
		}
	}

}
