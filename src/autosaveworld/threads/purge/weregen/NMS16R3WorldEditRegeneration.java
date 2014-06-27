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
import org.bukkit.craftbukkit.v1_6_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_6_R3.util.LongHash;

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

public class NMS16R3WorldEditRegeneration implements WorldEditRegenrationInterface {

	@Override
	public void regenerateRegion(World world, org.bukkit.util.Vector minpoint, org.bukkit.util.Vector maxpoint, RegenOptions options) {
		Vector minbpoint = BukkitUtil.toVector(minpoint);
		Vector maxbpoint = BukkitUtil.toVector(maxpoint);
		regenerateRegion(world, minbpoint, maxbpoint, options);
	}

	@Override
	@SuppressWarnings("deprecation")
	public void regenerateRegion(World world, Vector minpoint, Vector maxpoint, RegenOptions options) {
		BukkitWorld bw = new BukkitWorld(world);
		EditSession es = new EditSession(bw, Integer.MAX_VALUE);
		es.setFastMode(true);
		int maxy = bw.getMaxY() + 1;
		Region region = new CuboidRegion(bw, minpoint, maxpoint);
		LinkedList<BlockToPlaceBack> placeBackQueue = new LinkedList<BlockToPlaceBack>();

		//load all chunks
		for (Vector2D chunk : region.getChunks()) {
			world.getChunkAt(chunk.getBlockX(), chunk.getBlockZ()).load();
		}

		//now operate with them
		for (Vector2D chunk : region.getChunks()) {
			net.minecraft.server.v1_6_R3.WorldServer nmsWorld = ((CraftWorld)world).getHandle();
			Vector min = new Vector(chunk.getBlockX() * 16, 0, chunk.getBlockZ() * 16);
			int cx = chunk.getBlockX();
			int cz = chunk.getBlockZ();
			//save old chunk
			net.minecraft.server.v1_6_R3.Chunk oldnsmchunk = nmsWorld.chunkProviderServer.chunks.get(LongHash.toLong(cx, cz));
			//remove unsafe blocks
			if (options.shouldRemoveUnsafeBlocks()) {
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
			//check if chunk is fully inside the region
			boolean fullyinside = true;
			insidecheck:
			for (int x = 0; x < 16; ++x) {
				for (int y = 0; y < maxy; ++y) {
					for (int z = 0; z < 16; ++z) {
						Vector pt = min.add(x, y, z);
						if (!region.contains(pt)) {
							fullyinside = false;
							break insidecheck;
						}
					}
				}
			}
			//regenerate chunk
			world.regenerateChunk(cx, cz);
			//if chunk is not fully inside the region than copy needed data and then put old chunk
			if (!fullyinside) {
			    //save all generated data inside the region
				for (int x = 0; x < 16; ++x) {
					for (int y = 0; y < maxy; ++y) {
						for (int z = 0; z < 16; ++z) {
							Vector pt = min.add(x, y, z);
							if (region.contains(pt)) {
								placeBackQueue.add(new BlockToPlaceBack(pt, RegenerationUtils.getBlock(world, es, pt)));
							}
						}
					}
				}
				//put old chunk to map
				nmsWorld.chunkProviderServer.chunks.put(LongHash.toLong(cx, cz), oldnsmchunk);
			}
			//unload and load chunk
			world.unloadChunk(cx, cz, true, false);
			world.loadChunk(cx, cz);
			//refresh chunk
			world.refreshChunk(cx, cz);
		}

		//set all blocks that were inside the region back
		for (PlaceBackStage stage : RegenerationUtils.placeBackStages) {
			stage.processBlockPlaceBack(world, es, placeBackQueue);
		}
	}

}
