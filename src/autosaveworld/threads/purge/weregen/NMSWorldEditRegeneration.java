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

import java.util.Iterator;
import java.util.LinkedList;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import autosaveworld.threads.purge.weregen.WorldEditRegeneration.WorldEditRegenrationInterface;
import autosaveworld.threads.purge.weregen.nms.NMSAccess;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldguard.bukkit.BukkitUtil;

public class NMSWorldEditRegeneration implements WorldEditRegenrationInterface {

	private NMSAccess nms;
	public NMSWorldEditRegeneration(NMSAccess nms) {
		this.nms = nms;
	}

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
		int maxy = bw.getMaxY() + 1;
		Region region = new CuboidRegion(bw, minpoint, maxpoint);
		LinkedList<BlockToPlaceBack> placeBackQueue = new LinkedList<BlockToPlaceBack>();

		//now operate with them
		for (Vector2D chunk : region.getChunks()) {
			Vector min = new Vector(chunk.getBlockX() * 16, 0, chunk.getBlockZ() * 16);
			int cx = chunk.getBlockX();
			int cz = chunk.getBlockZ();
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
			if (fullyinside) {
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
				//regenerate chunk
				world.regenerateChunk(cx, cz);
			} else {
				//generate chunk
				Object generatedNMSChunk = nms.generateNMSChunk(world, cx, cz);
			    //save all generated data inside the region
				for (int x = 0; x < 16; ++x) {
					for (int y = 0; y < maxy; ++y) {
						for (int z = 0; z < 16; ++z) {
							Vector pt = min.add(x, y, z);
							if (region.contains(pt)) {
								placeBackQueue.add(new BlockToPlaceBack(pt, nms.getBlock(generatedNMSChunk, pt)));
							}
						}
					}
				}
			}
		}

		//set all blocks that were inside the region back
		for (PlaceBackStage stage : placeBackStages) {
			stage.processBlockPlaceBack(world, placeBackQueue);
		}

	}

	private static class BlockToPlaceBack {

		private Vector position;
		private NMSBlock block;
		public BlockToPlaceBack(Vector position, NMSBlock block) {
			this.position = position;
			this.block = block;
		}

		public Vector getPosition() {
			return position;
		}

		public NMSBlock getBlock() {
			return block;
		}

	}

	private PlaceBackStage[] placeBackStages = new PlaceBackStage[] {
		//normal stage place back
		new PlaceBackStage(
			new PlaceBackCheck() {
				@Override
				public boolean shouldPlaceBack(NMSBlock block) {
					return !BlockType.shouldPlaceLast(block.getTypeId()) && !BlockType.shouldPlaceFinal(block.getTypeId());
				}
			}
		),
		//last stage place back
		new PlaceBackStage(
			new PlaceBackCheck() {
				@Override
				public boolean shouldPlaceBack(NMSBlock block) {
					return BlockType.shouldPlaceLast(block.getTypeId());
				}
			}
		),
		//final stage place back
		new PlaceBackStage(
			new PlaceBackCheck() {
				@Override
				public boolean shouldPlaceBack(NMSBlock block) {
					return BlockType.shouldPlaceFinal(block.getTypeId());
				}
			}
		)
	};

	private interface PlaceBackCheck {
		public boolean shouldPlaceBack(NMSBlock block);
	}

	private class PlaceBackStage {

		private PlaceBackCheck check;
		public PlaceBackStage(PlaceBackCheck check) {
			this.check = check;
		}

		@SuppressWarnings("deprecation")
		public void processBlockPlaceBack(World world, LinkedList<BlockToPlaceBack> placeBackQueue) {
			Iterator<BlockToPlaceBack> entryit = placeBackQueue.iterator();
			while (entryit.hasNext()) {
				BlockToPlaceBack blockToPlaceBack = entryit.next();
				NMSBlock block = blockToPlaceBack.getBlock();
				if (check.shouldPlaceBack(block)) {
					Vector pt = blockToPlaceBack.getPosition();
					try {
						//set block to air to fix one really weird problem
						world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).setType(Material.AIR);
						//set block back if it is not air
						if (block.getTypeId() != Material.AIR.getId()) {
							block.set(nms, world, pt);
						}
					} catch (Throwable t) {
						t.printStackTrace();
					} finally {
						entryit.remove();
					}
				}
			}
		}

	}

	public static class NMSBlock {

		private int id;
		private byte data;
		private Object tileEntity;
		public NMSBlock(int id, byte data, Object tileEntity) {
			this.id = id;
			this.data = data;
			this.tileEntity = tileEntity;
		}

		public int getTypeId() {
			return id;
		}

		@SuppressWarnings("deprecation")
		public void set(NMSAccess nms, World world, Vector location) {
			world.getBlockAt(location.getBlockX(), location.getBlockY(), location.getBlockZ()).setTypeIdAndData(id, data, false);
			nms.setTileEntity(world, location, tileEntity);
		}

	}

}
