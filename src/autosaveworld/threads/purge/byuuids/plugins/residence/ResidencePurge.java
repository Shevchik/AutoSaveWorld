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

package autosaveworld.threads.purge.byuuids.plugins.residence;

import java.util.LinkedList;

import net.t00thpick1.residence.api.ResidenceAPI;
import net.t00thpick1.residence.api.areas.ResidenceArea;

import org.bukkit.Bukkit;
import org.bukkit.World;

import autosaveworld.core.logging.MessageLogger;
import autosaveworld.threads.purge.byuuids.ActivePlayersList;

public class ResidencePurge {

	@SuppressWarnings("deprecation")
	public void doResidencePurgeTask(ActivePlayersList activePlayersStorage, final boolean regenres) {

		MessageLogger.debug("Residence purge started");

		int deletedres = 0;

		LinkedList<ResidenceArea> reslist = new LinkedList<ResidenceArea>();
		for (World world : Bukkit.getWorlds()) {
			reslist.addAll(ResidenceAPI.getResidenceManager().getResidencesInWorld(world));
		}
		boolean wepresent = (Bukkit.getPluginManager().getPlugin("WorldEdit") != null);
		TaskQueue queue = new TaskQueue();

		// search for residences with inactive players
		for (final ResidenceArea resarea : reslist) {
			MessageLogger.debug("Checking residence " + resarea.getName());
			ResidenceRenterClearTask renterClearTask = null;
			if (resarea.isRented() && !activePlayersStorage.isActiveNameCS(resarea.getRenter())) {
				MessageLogger.debug("Renter "+resarea.getRenter()+" is inactive");
				renterClearTask = new ResidenceRenterClearTask(resarea);
			}
			if (!activePlayersStorage.isActiveNameCS(resarea.getOwner()) && ((renterClearTask != null) || !resarea.isRented())) {
				MessageLogger.debug("Owner "+resarea.getOwner()+" is inactive");
				// regen residence areas if needed
				if (regenres && wepresent) {
					ResidenceRegenTask regenTask = new ResidenceRegenTask(resarea);
					queue.addTask(regenTask);
				}
				// delete residence from db
				ResidenceDeleteTask deleteTask = new ResidenceDeleteTask(resarea);
				queue.addTask(deleteTask);

				deletedres += 1;
				continue;
			}
			// evict residence if needed
			if (renterClearTask != null) {
				queue.addTask(renterClearTask);
			}
		}
		// flush the rest of the queue
		queue.flush();

		MessageLogger.debug("Residence purge finished, deleted " + deletedres + " inactive residences");
	}

}