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

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;

import org.bukkit.World;
import org.bukkit.util.Vector;

import com.sk89q.worldedit.bukkit.BukkitUtil;

import autosaveworld.core.GlobalConstants;
import autosaveworld.modules.worldregen.SchematicData.SchematicToLoad;
import autosaveworld.modules.worldregen.SchematicData.SchematicToSave;
import autosaveworld.modules.worldregen.tasks.CopyDataProvider;
import autosaveworld.modules.worldregen.tasks.PasteDataProvider;

public class PStonesDataProvider implements CopyDataProvider, PasteDataProvider {

	private World wtoregen;

	public PStonesDataProvider(World wtoregen) {
		this.wtoregen = wtoregen;
	}

	@Override
	public List<SchematicToSave> getSchematicsToCopy() throws Exception {
		ArrayList<SchematicToSave> schematics = new ArrayList<SchematicToSave>();
		PreciousStones pstones = PreciousStones.getInstance();
		for (Field field : pstones.getForceFieldManager().getFields("*", wtoregen)) {
			String name = String.valueOf(field.getId());
			Vector min = new Vector(field.getMinx(), field.getMiny(), field.getMinz());
			Vector max = new Vector(field.getMaxx(), field.getMaxy(), field.getMaxz());
			schematics.add(new SchematicToSave(
				GlobalConstants.getPStonesTempFolder() + name,
				BukkitUtil.toVector(min), BukkitUtil.toVector(max),
				"Saving PreciousStones Region " + name + " to schematic",
				"PreciousStones Region " + name + " saved"
			));
		}
		return schematics;
	}

	@Override
	public List<SchematicToLoad> getSchematicsToPaste() throws Exception {
		ArrayList<SchematicToLoad> schematics = new ArrayList<SchematicToLoad>();
		PreciousStones pstones = PreciousStones.getInstance();
		for (Field field : pstones.getForceFieldManager().getFields("*", wtoregen)) {
			String name = String.valueOf(field.getId());
			schematics.add(new SchematicToLoad(
				GlobalConstants.getPStonesTempFolder() + name,
				"Pasting PreciousStones region " + name + " from schematic",
				"PreciousStones Region " + name + " pasted"
			));
		}
		return schematics;
	}

}
