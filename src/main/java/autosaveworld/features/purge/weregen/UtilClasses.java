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

package autosaveworld.features.purge.weregen;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;

public class UtilClasses {

	public static class BlockToPlaceBack {

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

	public static class ItemSpawnListener implements Listener {

		@EventHandler
		public void onItemSpawn(ItemSpawnEvent event) {
			event.setCancelled(true);
		}

	}

}
