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

package autosaveworld.features.purge.plugins.wg;

import org.bukkit.World;

import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import autosaveworld.core.logging.MessageLogger;
import autosaveworld.features.purge.taskqueue.Task;
import autosaveworld.features.purge.weregen.WorldEditRegeneration;

public class RegionRegenTask implements Task {

	private World world;
	private ProtectedRegion region;
	private boolean noregenoverlap;

	public RegionRegenTask(World world, ProtectedRegion region, boolean noregenoverlap) {
		this.world = world;
		this.region = region;
		this.noregenoverlap = noregenoverlap;
	}

	@Override
	public boolean doNotQueue() {
		return true;
	}

	@Override
	public void performTask() {
		RegionManager rm = WGBukkit.getRegionManager(world);
		if (!(noregenoverlap && (rm.getApplicableRegions(region).size() > 1))) {
			MessageLogger.debug("Regenerating region " + region.getId());
			WorldEditRegeneration.get().regenerateRegion(world, region.getMinimumPoint(), region.getMaximumPoint());
		}
	}

}
