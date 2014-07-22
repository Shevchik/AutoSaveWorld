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

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import autosaveworld.core.GlobalConstants;
import autosaveworld.core.logging.MessageLogger;
import autosaveworld.threads.worldregen.SchematicOperations;
import autosaveworld.threads.worldregen.SchematicData.SchematicToSave;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.bukkit.BukkitUtil;

public class PStonesCopy {

	private World wtoregen;
	public PStonesCopy(String worldtoregen) {
		this.wtoregen = Bukkit.getWorld(worldtoregen);
	}

	public void copyAllToSchematics() {
		MessageLogger.debug("Saving preciousstones regions to schematics");

		PreciousStones pstones = PreciousStones.getInstance();

		HashSet<Field> fields = new HashSet<Field>();
		for (Field field : pstones.getForceFieldManager().getFields("*", wtoregen)) {
			if (field.isParent()) {
				fields.add(field);
			}
		}

		

	}

}
