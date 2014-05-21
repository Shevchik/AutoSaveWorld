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

package autosaveworld.threads.worldregen;

import java.io.File;

import org.bukkit.World;

import com.sk89q.worldedit.Vector;

public class SchematicData {

	public static class SchematicToSave {

		private File file;
		private World world;
		private Vector bvmin;
		private Vector bvmax;

		public SchematicToSave(String filepath, World world, Vector bvmin, Vector bvmax) {
			this.file = new File(filepath);
			this.world = world;
			this.bvmin = bvmin;
			this.bvmax = bvmax;
		}

		public File getFile() {
			return file;
		}

		public World getWorld() {
			return world;
		}

		public Vector getMin() {
			return bvmin;
		}

		public Vector getMax() {
			return bvmax;
		}

	}

	public static class SchematicToLoad {

		private File file;
		private World world;

		public SchematicToLoad(String filepath, World world) {
			this.file = new File(filepath);
			this.world = world;
		}

		public File getFile() {
			return file;
		}

		public World getWorld() {
			return world;
		}

	}

}
