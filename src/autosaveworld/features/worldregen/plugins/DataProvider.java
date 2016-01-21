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

package autosaveworld.features.worldregen.plugins;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.World;

import autosaveworld.features.worldregen.storage.Coord;

public abstract class DataProvider {

	private final LinkedList<Coord> chunks = new LinkedList<Coord>();

	protected World world;

	public DataProvider(World world) throws Throwable {
		this.world = world;
		init();
	}

	public final List<Coord> getChunks() {
		return chunks;
	}

	protected abstract void init() throws Throwable;

	protected final void addChunkAtCoord(int chunkX, int chunkZ) {
		chunks.add(new Coord(chunkX, chunkZ));
	}

	protected final void addChunksInBounds(int worldXMin, int worldZMin, int worldXMax, int worldZMax) {
		for (int x = (worldXMin >> 4); x <= (worldXMax >> 4); x++) {
			for (int z = (worldZMin >> 4); z <= (worldZMax >> 4); z++) {
				chunks.add(new Coord(x, z));
			}
		}
	}

}
