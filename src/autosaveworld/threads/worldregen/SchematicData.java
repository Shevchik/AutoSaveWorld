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

import com.sk89q.worldedit.Vector;

public class SchematicData {

	public static class SchematicToSave {

		private File file;
		private Vector bvmin;
		private Vector bvmax;

		public SchematicToSave(String filepath, Vector bvmin, Vector bvmax) {
			this.file = new File(filepath);
			this.bvmin = bvmin;
			this.bvmax = bvmax;
		}

		public File getFile() {
			return file;
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

		public SchematicToLoad(String filepath) {
			this.file = new File(filepath);
		}

		public File getFile() {
			return file;
		}

	}

}
