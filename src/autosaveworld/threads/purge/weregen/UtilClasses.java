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

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockType;

public class UtilClasses {

	protected static PlaceBackStage[] placeBackStages = new PlaceBackStage[] {
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

	protected static class BlockToPlaceBack {

		private Vector position;
		private BaseBlock block;
		public BlockToPlaceBack(Vector position, BaseBlock block) {
			this.position = position;
			this.block = block;
		}

		public Vector getPosition() {
			return position;
		}

		public BaseBlock getBlock() {
			return block;
		}

	}

	protected static class PlaceBackStage {

		public static interface PlaceBackCheck {
			public boolean shouldPlaceBack(BaseBlock block);
		}

		private PlaceBackCheck check;
		public PlaceBackStage(PlaceBackCheck check) {
			this.check = check;
		}

		public void processBlockPlaceBack(World world, EditSession es, boolean settoairfirst, LinkedList<BlockToPlaceBack> placeBackQueue) {
			Iterator<BlockToPlaceBack> entryit = placeBackQueue.iterator();
			while (entryit.hasNext()) {
				BlockToPlaceBack blockToPlaceBack = entryit.next();
				BaseBlock block = blockToPlaceBack.getBlock();
				if (check.shouldPlaceBack(block)) {
					Vector pt = blockToPlaceBack.getPosition();
					try {
						if (settoairfirst) {
							//set block to air to fix one really weird problem
							world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).setType(Material.AIR);
						}
						//set block back
						es.rawSetBlock(pt, block);
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
