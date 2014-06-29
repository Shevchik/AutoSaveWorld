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

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;

import autosaveworld.threads.purge.weregen.WorldEditRegeneration.WorldEditRegenrationInterface;
import autosaveworld.threads.purge.weregen.nms.NMSAccess;
import autosaveworld.utils.ListenerUtils;

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

	private ItemSpawnListener listener = new ItemSpawnListener();

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
		LinkedList<NMSBlock> placeQueue = new LinkedList<NMSBlock>();

		for (Vector2D chunk : region.getChunks()) {
			Vector min = new Vector(chunk.getBlockX() * 16, 0, chunk.getBlockZ() * 16);
			int cx = chunk.getBlockX();
			int cz = chunk.getBlockZ();
			//generate chunk
			Object generatedNMSChunk = nms.generateNMSChunk(world, cx, cz);
		    //save all generated data inside the region
			for (int x = 0; x < 16; ++x) {
				for (int y = 0; y < maxy; ++y) {
					for (int z = 0; z < 16; ++z) {
						Vector pt = min.add(x, y, z);
						if (region.contains(pt)) {
							placeQueue.add(nms.getBlock(generatedNMSChunk, pt).addLocationInfo(pt));
						}
					}
				}
			}
		}

		//register listener that will prevent block drop from spawning
		ListenerUtils.registerListener(listener);

		//set all blocks that were inside the region back
		for (PlaceBackStage stage : placeBackStages) {
			stage.processBlockPlaceBack(world, placeQueue);
		}

		//unregister listener
		ListenerUtils.unregisterListener(listener);
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

		public void processBlockPlaceBack(World world, LinkedList<NMSBlock> placeBackQueue) {
			Iterator<NMSBlock> entryit = placeBackQueue.iterator();
			while (entryit.hasNext()) {
				NMSBlock block = entryit.next();
				if (check.shouldPlaceBack(block)) {
					try {
						nms.setBlock(world, block.getLocation(), block);
					} catch (Throwable t) {
						t.printStackTrace();
					} finally {
						entryit.remove();
					}
				}
			}
		}

	}

	private class ItemSpawnListener implements Listener {

		@EventHandler
		public void onItemSpawn(ItemSpawnEvent event) {
			event.setCancelled(true);
		}

	}

	public static class NMSBlock {

		private Vector location;
		private int id;
		private byte data;
		private Object tileEntity;
		public NMSBlock(int id, byte data, Object tileEntity) {
			this.id = id;
			this.data = data;
			this.tileEntity = tileEntity;
		}

		public NMSBlock addLocationInfo(Vector location) {
			this.location = location;
			return this;
		}

		public Vector getLocation() {
			return location;
		}

		public int getTypeId() {
			return id;
		}

		public byte getData() {
			return data;
		}

		public Object getTileEntitiy() {
			return tileEntity;
		}

	}

}
