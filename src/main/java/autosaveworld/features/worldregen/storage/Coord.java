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

public class Coord {

	private final int x;
	private final int z;
	public Coord(int x, int z) {
		this.x = x;
		this.z = z;
	}

	public int getX() {
		return x;
	}

	public int getZ() {
		return z;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Coord)) {
			return false;
		}
		Coord othercoord = (Coord) other;
		return x == othercoord.x && z == othercoord.z;
	}

	@Override
	public int hashCode() {
		return x * 31 + z;
	}

	@Override
	public String toString() {
		return getX()+"."+getZ();
	}

	public static int getRegionCoord(int worldCoord) {
		return worldCoord >> 5;
	}

	public static int getLocalCoord(int worldCoord) {
		return worldCoord & 31;
	}

}