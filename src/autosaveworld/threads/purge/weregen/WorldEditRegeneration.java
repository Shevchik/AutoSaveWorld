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

import org.bukkit.Bukkit;
import org.bukkit.World;

import autosaveworld.core.logging.MessageLogger;
import autosaveworld.threads.purge.weregen.nms.NMS16R3Access;
import autosaveworld.threads.purge.weregen.nms.NMS17R1Access;
import autosaveworld.threads.purge.weregen.nms.NMS17R2Access;
import autosaveworld.threads.purge.weregen.nms.NMS17R3Access;

import com.sk89q.worldedit.Vector;

public class WorldEditRegeneration {

	private static WorldEditRegenrationInterface instance;
	public static WorldEditRegenrationInterface get() {
		if (instance == null) {
			/*String packageName = Bukkit.getServer().getClass().getPackage().getName();
			String nmspackageversion = packageName.substring(packageName.lastIndexOf('.') + 1);
			switch (nmspackageversion) {
				case "v1_7_R3": {
					instance = new NMSWorldEditRegeneration(new NMS17R3Access());
					MessageLogger.debug("Using NMS17R3 WorldEdit regeneration");
					break;
				}
				case "v1_7_R2": {
					instance = new NMSWorldEditRegeneration(new NMS17R2Access());
					MessageLogger.debug("Using NMS17R2 WorldEdit regeneration");
					break;
				}
				case "v1_7_R1": {
					instance = new NMSWorldEditRegeneration(new NMS17R1Access());
					MessageLogger.debug("Using NMS17R1 WorldEdit regeneration");
					break;
				}
				case "v1_6_R3": {
					instance = new NMSWorldEditRegeneration(new NMS16R3Access());
					MessageLogger.debug("Using NMS16R3 WorldEdit regeneration");
					break;
				}
			}*/
			if (instance == null) {
				instance = new BukkitAPIWorldEditRegeneration();
				MessageLogger.debug("Using BukkitAPI WorldEdit regeneration");
			}
		}
		return instance;
	}

	public static interface WorldEditRegenrationInterface {
		public void regenerateRegion(World world, org.bukkit.util.Vector minpoint, org.bukkit.util.Vector maxpoint, RegenOptions options);
		public void regenerateRegion(World world, Vector minpoint, Vector maxpoint, RegenOptions options);
	}

}
