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

package autosaveworld.threads.purge;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldguard.bukkit.BukkitUtil;

public class WorldEditRegeneration {

	public static void regenerateRegion(World world, org.bukkit.util.Vector minpoint, org.bukkit.util.Vector maxpoint, RegenOptions options) {
		Vector minbpoint = BukkitUtil.toVector(minpoint);
		Vector maxbpoint = BukkitUtil.toVector(maxpoint);
		regenerateRegion(world, minbpoint, maxbpoint, options);
	}

	@SuppressWarnings("deprecation")
	public static void regenerateRegion(World world, Vector minpoint, Vector maxpoint, RegenOptions options) {
		BukkitWorld bw = new BukkitWorld(world);
		int maxy = bw.getMaxY() + 1;
		Region region = new CuboidRegion(bw, minpoint, maxpoint);
		HashMap<Vector, BaseBlock> placeBackQueue = new HashMap<Vector, BaseBlock>(500);
		//first save all blocks that are inside affected chunks but outside the region
		for (Vector2D chunk : region.getChunks()) {
			Vector min = new Vector(chunk.getBlockX() * 16, 0, chunk.getBlockZ() * 16);
			for (int x = 0; x < 16; ++x) {
				for (int y = 0; y < maxy; ++y) {
					for (int z = 0; z < 16; ++z) {
						Vector pt = min.add(x, y, z);
						if (!region.contains(pt)) {
							placeBackQueue.put(pt, bw.getBlock(pt));
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
		for (PlaceBackStage stage : placeBackStages) {
			stage.processBlockPlaceBack(world, bw, placeBackQueue);
		}
	}

	public static class RegenOptions {

		private boolean removeunsafeblocks = false;
		private boolean[] safelist = new boolean[4096];

		public RegenOptions() {
		}

		public RegenOptions(Set<Integer> safeblocks) {
			if (safeblocks.isEmpty()) {
				return;
			}
			removeunsafeblocks = true;
			for (int safeblockid : safeblocks) {
				safelist[safeblockid] = true;
			}
		}

		public boolean shouldRemoveUnsafeBlocks() {
			return removeunsafeblocks;
		}

		public boolean isBlockSafe(int id) {
			return safelist[id];
		}

		public static HashSet<Integer> parseListToIDs(Set<String> list) {
			HashSet<Integer> set = new HashSet<Integer>();
			for (String element : list) {
				if (element.contains("-")) {
					try {
						String[] split = element.split("[-]");
						int start = Integer.parseInt(split[0]);
						int end = Integer.parseInt(split[1]);
						for (int i = start; i <= end; i++) {
							set.add(i);
						}
					} catch (Exception e) {
					}
				} else {
					try {
						set.add(Integer.parseInt(element));
					} catch (Exception e) {
					}
				}
			}
			return set;
		}

	}

	private static PlaceBackStage[] placeBackStages = new PlaceBackStage[] {
		//normal stage place back
		new PlaceBackStage(
			new PlaceBackStage.PlaceBackCheck() {
				@Override
				public boolean shouldPlaceBack(BaseBlock block) {
					return !BlockType.shouldPlaceLast(block.getId()) && !BlockType.shouldPlaceFinal(block.getId());
				}
			}
		),
		//last stage place back
		new PlaceBackStage(
			new PlaceBackStage.PlaceBackCheck() {
				@Override
				public boolean shouldPlaceBack(BaseBlock block) {
					return BlockType.shouldPlaceLast(block.getId());
				}
			}
		),
		//final stage place back
		new PlaceBackStage(
			new PlaceBackStage.PlaceBackCheck() {
				@Override
				public boolean shouldPlaceBack(BaseBlock block) {
					return BlockType.shouldPlaceFinal(block.getId());
				}
			}
		)
	};

	private static class PlaceBackStage {

		public static interface PlaceBackCheck {
			public boolean shouldPlaceBack(BaseBlock block);
		}

		private PlaceBackCheck check;
		public PlaceBackStage(PlaceBackCheck check) {
			this.check = check;
		}

		public void processBlockPlaceBack(World world, BukkitWorld bw, HashMap<Vector, BaseBlock> placeBackQueue) {
			Iterator<Entry<Vector, BaseBlock>> entryit = placeBackQueue.entrySet().iterator();
			while (entryit.hasNext()) {
				Entry<Vector, BaseBlock> placeBackEntry = entryit.next();
				BaseBlock block = placeBackEntry.getValue();
				if (check.shouldPlaceBack(block)) {
					Vector pt = placeBackEntry.getKey();
					try {
						//set block to air to fix one really weird problem
						world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).setType(Material.AIR);
						//set block back
						bw.setBlock(pt, block, false);
					} catch (Throwable t) {
						t.printStackTrace();
					} finally {
						entryit.remove();
					}
				}
			}
		}

	}

}
