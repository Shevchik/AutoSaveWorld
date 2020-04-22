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

package autosaveworld.features.worldregen.storage;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class WorldMap {

	private final HashMap<Coord, Set<Coord>> map = new HashMap<Coord, Set<Coord>>(200);

	private int growRadius;

	public WorldMap(int growRadius) {
		this.growRadius = growRadius;
	}

	public void addChunk(Coord chunkCoord) {
		addChunk0(chunkCoord);
		if (growRadius > 0) {
			for (int xAdd = -growRadius; xAdd <= growRadius; xAdd++) {
				for (int zAdd = -growRadius; zAdd <= growRadius; zAdd++) {
					addChunk0(new Coord(chunkCoord.getX() + xAdd, chunkCoord.getZ() + zAdd));
				}
			}
		}
	}

	private void addChunk0(Coord chunkCoord) {
		getChunks0(Coord.getRegionCoord(chunkCoord.getX()), Coord.getRegionCoord(chunkCoord.getZ()))
		.add(new Coord(Coord.getLocalCoord(chunkCoord.getX()), Coord.getLocalCoord(chunkCoord.getZ())));
	}

	public boolean hasChunks(int regionX, int regionZ) {
		return map.containsKey(new Coord(regionX, regionZ));
	}

	public Set<Coord> getChunks(int regionX, int regionZ) {
		return Collections.unmodifiableSet(getChunks0(regionX, regionZ));
	}

	private Set<Coord> getChunks0(int regionX, int regionZ) {
		Coord regionCoord = new Coord(regionX, regionZ);
		Set<Coord> chunks = map.get(regionCoord);
		if (chunks == null) {
			chunks = new HashSet<Coord>(40);
			map.put(regionCoord, chunks);
		}
		return chunks;
	}

}
