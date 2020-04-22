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

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;

import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import autosaveworld.core.AutoSaveWorld;
import autosaveworld.core.logging.MessageLogger;
import autosaveworld.features.purge.ActivePlayersList;
import autosaveworld.features.purge.DataPurge;
import autosaveworld.features.purge.taskqueue.TaskExecutor;

public class WGPurge extends DataPurge {

	public WGPurge(ActivePlayersList activelist) {
		super("WorldGuard", activelist);
	}

	@Override
	public void doPurge() {
		try (TaskExecutor queue = new TaskExecutor(30)) {
			for (World w : Bukkit.getWorlds()) {
				MessageLogger.debug("Checking WG protections in world " + w.getName());
				RegionManager regionmanager = WGBukkit.getRegionManager(w);
				ArrayList<ProtectedRegion> regions = new ArrayList<ProtectedRegion>(regionmanager.getRegions().values());
				for (ProtectedRegion rg : regions) {
					MessageLogger.debug("Checking region " + rg.getId());
					// skip region with zero owners and members
					if (!rg.hasMembersOrOwners()) {
						continue;
					}
					// check players
					DomainClearTask domainClearTask = new DomainClearTask(rg);
					for (DefaultDomain domain : new DefaultDomain[] {rg.getOwners(), rg.getMembers()}) {
						for (String playerName : domain.getPlayers()) {
							if (!activeplayerslist.isActiveName(playerName)) {
								MessageLogger.debug(playerName + " is inactive");
								domainClearTask.add(playerName);
							}
						}
						for (UUID playerUUID : domain.getUniqueIds()) {
							if (!activeplayerslist.isActiveUUID(playerUUID)) {
								MessageLogger.debug(playerUUID + " is inactive");
								domainClearTask.add(playerUUID);
							}
						}
					}
					// remove region if all owners and members are inactive
					if (domainClearTask.getPlayersToClearCount() == (rg.getOwners().size() + rg.getMembers().size())) {
						// regen region if needed
						if (AutoSaveWorld.getInstance().getMainConfig().purgeWGRegenRg) {
							RegionRegenTask regenTask = new RegionRegenTask(w, rg, AutoSaveWorld.getInstance().getMainConfig().purgeWGNoregenOverlap);
							queue.execute(regenTask);
						}
						// delete region
						RegionDeleteTask deleteTask = new RegionDeleteTask(w, rg);
						queue.execute(deleteTask);
						incDeleted();
						continue;
					}
					// cleanup region default domain if we have something to cleanup
					if (domainClearTask.hasPlayersToClear()) {
						queue.execute(domainClearTask);
						incCleaned();
					}
				}
			}
		}
	}

}
