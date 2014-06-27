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

package autosaveworld.threads.purge.weregen;

import java.util.LinkedList;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import autosaveworld.threads.purge.weregen.RegenerationUtils.BlockToPlaceBack;
import autosaveworld.threads.purge.weregen.RegenerationUtils.PlaceBackStage;
import autosaveworld.threads.purge.weregen.WorldEditRegeneration.WorldEditRegenrationInterface;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldguard.bukkit.BukkitUtil;

public class BukkitAPIWorldEditRegeneration implements WorldEditRegenrationInterface {

	public void regenerateRegion(World world, org.bukkit.util.Vector minpoint, org.bukkit.util.Vector maxpoint, RegenOptions options) {
		Vector minbpoint = BukkitUtil.toVector(minpoint);
		Vector maxbpoint = BukkitUtil.toVector(maxpoint);
		regenerateRegion(world, minbpoint, maxbpoint, options);
	}

	@SuppressWarnings("deprecation")
	public void regenerateRegion(World world, Vector minpoint, Vector maxpoint, RegenOptions options) {
		BukkitWorld bw = new BukkitWorld(world);
		EditSession es = new EditSession(bw, Integer.MAX_VALUE);
		es.setFastMode(true);
		int maxy = bw.getMaxY() + 1;
		Region region = new CuboidRegion(bw, minpoint, maxpoint);
		LinkedList<BlockToPlaceBack> placeBackQueue = new LinkedList<BlockToPlaceBack>();
		//first save all blocks that are inside affected chunks but outside the region
		for (Vector2D chunk : region.getChunks()) {
			Vector min = new Vector(chunk.getBlockX() * 16, 0, chunk.getBlockZ() * 16);
			for (int x = 0; x < 16; ++x) {
				for (int y = 0; y < maxy; ++y) {
					for (int z = 0; z < 16; ++z) {
						Vector pt = min.add(x, y, z);
						if (!region.contains(pt)) {
							placeBackQueue.add(new BlockToPlaceBack(pt, RegenerationUtils.getBlock(world, es, pt)));
						}
					}
				}
			}
		}

		//remove all unsafe blocks
		if (options.shouldRemoveUnsafeBlocks()) {
			for (Vector2D chunk : region.getChunks()) {
				Vector min = new Vector(chunk.getBlockX() * 16, 0, chunk.getBlockZ() * 16);
				for (int x = 0; x < 16; ++x) {
					for (int y = 0; y < maxy; ++y) {
						for (int z = 0; z < 16; ++z) {
							Vector pt = min.add(x, y, z);
							Block block = world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
							if (!options.isBlockSafe(block.getTypeId())) {
								block.setType(Material.AIR);
							}
						}
					}
				}
			}
		}

		//regenerate all affected chunks
		for (Vector2D chunk : region.getChunks()) {
			try {
				world.regenerateChunk(chunk.getBlockX(), chunk.getBlockZ());
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}

		//set all blocks that were outside the region back
		for (PlaceBackStage stage : RegenerationUtils.placeBackStages) {
			stage.processBlockPlaceBack(world, es, placeBackQueue);
		}
	}

}
