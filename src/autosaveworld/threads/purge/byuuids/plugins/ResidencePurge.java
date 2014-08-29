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

package autosaveworld.threads.purge.byuuids.plugins;

import java.util.LinkedList;

import net.t00thpick1.residence.api.ResidenceAPI;
import net.t00thpick1.residence.api.areas.ResidenceArea;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.util.Vector;

import autosaveworld.core.logging.MessageLogger;
import autosaveworld.threads.purge.byuuids.ActivePlayersList;
import autosaveworld.threads.purge.weregen.WorldEditRegeneration;
import autosaveworld.utils.SchedulerUtils;

public class ResidencePurge {

	@SuppressWarnings("deprecation")
	public void doResidencePurgeTask(ActivePlayersList pacheck, final boolean regenres) {

		MessageLogger.debug("Residence purge started");

		int deletedres = 0;

		LinkedList<ResidenceArea> reslist = new LinkedList<ResidenceArea>();
		for (World world : Bukkit.getWorlds()) {
			reslist.addAll(ResidenceAPI.getResidenceManager().getResidencesInWorld(world));
		}
		boolean wepresent = (Bukkit.getPluginManager().getPlugin("WorldEdit") != null);

		// search for residences with inactive players
		for (final ResidenceArea resarea : reslist) {
			MessageLogger.debug("Checking residence " + resarea.getName());
			if (!pacheck.isActiveNameCS(resarea.getOwner()) && !pacheck.isActiveNameCS(resarea.getRenter())) {
				MessageLogger.debug("Owner and renter of residence " + resarea.getName() + " is inactive. Purging residence");

				// regen residence areas if needed
				if (regenres && wepresent) {
					Runnable resarearegen = new Runnable() {
						Vector minpoint = resarea.getLowLocation().toVector();
						Vector maxpoint = resarea.getHighLocation().toVector();

						@Override
						public void run() {
							MessageLogger.debug("Regenerating residence " + resarea.getName());
							WorldEditRegeneration.get().regenerateRegion(resarea.getWorld(), minpoint, maxpoint);
						}
					};
					SchedulerUtils.callSyncTaskAndWait(resarearegen);
				}
				// delete residence from db
				MessageLogger.debug("Deleting residence " + resarea.getName());
				Runnable delres = new Runnable() {
					@Override
					public void run() {
						ResidenceAPI.getResidenceManager().remove(resarea);
					}
				};
				SchedulerUtils.callSyncTaskAndWait(delres);

				deletedres += 1;
			}
		}

		MessageLogger.debug("Residence purge finished, deleted " + deletedres + " inactive residences");
	}

}
