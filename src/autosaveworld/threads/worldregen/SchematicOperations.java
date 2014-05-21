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

package autosaveworld.threads.worldregen;

import java.io.IOException;

import autosaveworld.threads.worldregen.SchematicData.SchematicToLoad;
import autosaveworld.threads.worldregen.SchematicData.SchematicToSave;
import autosaveworld.utils.SchedulerUtils;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalEntity;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.schematic.SchematicFormat;

public class SchematicOperations {

	public static void saveToSchematic(final SchematicToSave schematicdata) {
		Runnable copypaste = new Runnable() {
			@Override
			public void run() {
				try {
					//create clipboard
					BukkitWorld bw = new BukkitWorld(schematicdata.getWorld());
					EditSession es = new EditSession(bw, Integer.MAX_VALUE);
					CuboidClipboard clipboard = new CuboidClipboard(
						schematicdata.getMax().subtract(schematicdata.getMin()).add(new Vector(1, 1, 1)),
						schematicdata.getMin(),
						schematicdata.getMin().subtract(schematicdata.getMax())
					);
					Region region = new CuboidRegion(bw, schematicdata.getMin(), schematicdata.getMax());
					es.setFastMode(true);
					//copy blocks
					clipboard.copy(es);
					//copy entities (note: worldedit doesn't save entities to schematic, but i will just leave it here in case worldedit will start doing it some day)
					LocalEntity[] entities = bw.getEntities(region);
					for (LocalEntity entity : entities) {
						clipboard.storeEntity(entity);
					}
					//save to schematic
					SchematicFormat.MCEDIT.save(clipboard, schematicdata.getFile());
				} catch (IOException | DataException e) {
					e.printStackTrace();
				}
			}
		};
		SchedulerUtils.callSyncTaskAndWait(copypaste);
	}

	public static void pasteFromSchematic(final SchematicToLoad schematicdata) {
		try {
			//load from schematic to clipboard
			final EditSession es = new EditSession(new BukkitWorld(schematicdata.getWorld()),Integer.MAX_VALUE);
			es.setFastMode(true);
			es.enableQueue();
			final CuboidClipboard cc = SchematicFormat.MCEDIT.load(schematicdata.getFile());
			//get schematic coords
			final Vector size = cc.getSize();
			final Vector origin = cc.getOrigin();
			Runnable genchunks = new Runnable() {
				@Override
				public void run() {
					//generate chunks at schematic position and 3 chunk radius nearby
					for (int x = -16*3; x < size.getBlockX() + 16*3; x+=16) {
						for (int z = -16*3; z < size.getBlockZ() + 16*3; z+=16) {
							//getChunkAt automatically loads chunk
							schematicdata.getWorld().getChunkAt(origin.getBlockX()+x, origin.getBlockZ()+z).load();
						}
					}
				}
			};
			SchedulerUtils.callSyncTaskAndWait(genchunks);
			Runnable paste = new Runnable() {
				@Override
				public void run() {
					//paste blocks
					for (int x = 0; x < size.getBlockX(); ++x) {
						for (int y = 0; y < size.getBlockY(); ++y) {
							for (int z = 0; z < size.getBlockZ(); ++z) {
								Vector blockpos = new Vector(x, y, z);
								final BaseBlock block = cc.getBlock(blockpos);

								if (block == null) {
									continue;
								}

								try {
									es.smartSetBlock(new Vector(x, y, z).add(origin), block);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}
					}
					es.flushQueue();
					//paste entities (note: worldedit doesn't paste entities from schematic, but i will just leave it here in case worldedit will start doing it some day)
					try {
						cc.pasteEntities(origin);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};
			SchedulerUtils.callSyncTaskAndWait(paste);
		} catch (IOException | DataException e) {
			e.printStackTrace();
		}
	}

}
