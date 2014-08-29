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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;

import autosaveworld.core.logging.MessageLogger;
import autosaveworld.threads.purge.byuuids.ActivePlayersList;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class WGPurge {

	public void doWGPurgeTask(ActivePlayersList pacheck, final boolean regenrg, boolean noregenoverlap) {

		MessageLogger.debug("WG purge started");

		WorldGuardPlugin wg = (WorldGuardPlugin) Bukkit.getPluginManager().getPlugin("WorldGuard");

		int deletedrg = 0;

		for (final World w : Bukkit.getWorlds()) {
			MessageLogger.debug("Checking WG protections in world " + w.getName());
			final RegionManager m = wg.getRegionManager(w);

			// search for inactive players in regions
			HashSet<ProtectedRegion> regions = new HashSet<ProtectedRegion>(m.getRegions().values());
			TaskQueue queue = new TaskQueue(w);
			for (final ProtectedRegion rg : regions) {
				MessageLogger.debug("Checking region " + rg.getId());
				// skip region with zero owners and members
				if (!rg.hasMembersOrOwners()) {
					continue;
				}
				// check players
				DomainClearTask domainClearTask = new DomainClearTask(rg);
				ArrayList<DefaultDomain> domains = new ArrayList<DefaultDomain>();
				domains.add(rg.getOwners());
				domains.add(rg.getMembers());
				int inactive = 0;
				for (DefaultDomain domain : domains) {
					for (String playerName : domain.getPlayers()) {
						if (!pacheck.isActiveNameNCS(playerName)) {
							MessageLogger.debug(playerName + " is inactive");
							domainClearTask.add(playerName);
							inactive++;
						}
					}
					for (UUID playerUUID : domain.getUniqueIds()) {
						if (!pacheck.isActiveUUID(playerUUID)) {
							MessageLogger.debug(playerUUID + " is inactive");
							domainClearTask.add(playerUUID);
							inactive++;
						}
					}
				}
				// remove region if all owners and members are inactive
				if (inactive == (rg.getOwners().size() + rg.getMembers().size())) {
					if (regenrg) {
						RegionRegenTask regenTask = new RegionRegenTask(rg, noregenoverlap);
						queue.addTask(regenTask);
					}
					RegionDeleteTask deleteTask = new RegionDeleteTask(rg);
					queue.addTask(deleteTask);
					deletedrg += 1;
					continue;
				}
				// cleanup region default domain if we have sometihng to cleanup
				if (domainClearTask.hasPlayersToClear()) {
					queue.addTask(domainClearTask);
				}
			}
			// flush the rest of the queue
			queue.flush();
		}

		MessageLogger.debug("WG purge finished, deleted " + deletedrg + " inactive regions");
	}

}
