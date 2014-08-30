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

package autosaveworld.threads.purge.byuuids.plugins.wg;

import org.bukkit.World;

import autosaveworld.core.logging.MessageLogger;
import autosaveworld.threads.purge.weregen.WorldEditRegeneration;

import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class RegionRegenTask implements WGPurgeTask {

	private ProtectedRegion region;
	private boolean noregenoverlap;

	public RegionRegenTask(ProtectedRegion region, boolean noregenoverlap) {
		this.region = region;
		this.noregenoverlap = noregenoverlap;
	}

	@Override
	public boolean isHeavyTask() {
		return true;
	}

	@Override
	public void performTask(World world) {
		RegionManager rm = WGBukkit.getRegionManager(world);
		if (!(noregenoverlap && (rm.getApplicableRegions(region).size() > 1))) {
			MessageLogger.debug("Regenerating region " + region.getId());
			WorldEditRegeneration.get().regenerateRegion(world, region.getMinimumPoint(), region.getMaximumPoint());
		}
	}

}
