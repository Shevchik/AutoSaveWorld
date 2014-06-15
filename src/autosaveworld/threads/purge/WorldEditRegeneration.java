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

import org.bukkit.Material;
import org.bukkit.World;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldguard.bukkit.BukkitUtil;

public class WorldEditRegeneration {

	public static void regenerateRegion(World world, org.bukkit.util.Vector minpoint, org.bukkit.util.Vector maxpoint) {
		Vector minbpoint = BukkitUtil.toVector(minpoint);
		Vector maxbpoint = BukkitUtil.toVector(maxpoint);
		regenerateRegion(world, minbpoint, maxbpoint);
	}

	public static void regenerateRegion(World world, Vector minpoint, Vector maxpoint) {
		BukkitWorld bw = new BukkitWorld(world);
		int maxy = bw.getMaxY() + 1;
		Region region = new CuboidRegion(bw, minpoint, maxpoint);
		BaseBlock[] history = new BaseBlock[16 * 16 * maxy];
		for (Vector2D chunk : region.getChunks()) {
			//save all the blocks inside the chunk
			Vector min = new Vector(chunk.getBlockX() * 16, 0, chunk.getBlockZ() * 16);
			for (int x = 0; x < 16; ++x) {
				for (int y = 0; y < maxy; ++y) {
					for (int z = 0; z < 16; ++z) {
						Vector pt = min.add(x, y, z);
						if (!region.contains(pt)) {
							int index = y * 16 * 16 + z * 16 + x;
							history[index] = bw.getBlock(pt);
						}
					}
				}
			}

			//regenerate chunk
			try {
				world.regenerateChunk(chunk.getBlockX(), chunk.getBlockZ());
			} catch (Throwable t) {
				t.printStackTrace();
			}

			//then restore blocks inside the chunk but outside the region
			for (int x = 0; x < 16; ++x) {
				for (int y = 0; y < maxy; ++y) {
					for (int z = 0; z < 16; ++z) {
						Vector pt = min.add(x, y, z);
						int index = y * 16 * 16 + z * 16 + x;
						if (!region.contains(pt)) {
							world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).setType(Material.AIR); //temporal fix for blocks not setting properly if chunk wasn't loaded (WTF?)
							bw.setBlock(pt, history[index], false);
						}
					}
				}
			}
		}
	}

}
