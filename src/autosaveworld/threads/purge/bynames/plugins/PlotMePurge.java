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

package autosaveworld.threads.purge.bynames.plugins;

import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.World;

import autosaveworld.core.logging.MessageLogger;
import autosaveworld.threads.purge.bynames.ActivePlayersList;
import autosaveworld.utils.SchedulerUtils;

import com.worldcretornica.plotme.Plot;
import com.worldcretornica.plotme.PlotManager;
import com.worldcretornica.plotme.SqlManager;

public class PlotMePurge {

	public void doPlotMePurgeTask(ActivePlayersList pacheck, final boolean regenplot) {

		MessageLogger.debug("PlotMe purge started");

		int delplots = 0;

		for (final World w : Bukkit.getWorlds()) {
			if (PlotManager.isPlotWorld(w)) {
				MessageLogger.debug("Checking plots in world "+w.getName().toLowerCase());

				//search plot for inactive owners
				HashSet<Plot> plots = new HashSet<Plot>(PlotManager.getPlots(w).values());
				for (final Plot p : plots) {
					final String PlotID = p.id;
					MessageLogger.debug("Checking plot " + PlotID);

					if (!pacheck.isActiveCS(p.getOwner())) {
						MessageLogger.debug("Plot owner is inactive. Purging plot "+PlotID);

						Runnable delPlot = new Runnable() {
							@Override
							public void run() {
								if (regenplot) {
									MessageLogger.debug("Regenerating plot "+PlotID);
									PlotManager.clear(w, p);
								}
								MessageLogger.debug("Deleting plot "+PlotID);
								PlotManager.getPlots(w).remove(PlotID);

								PlotManager.removeOwnerSign(w, PlotID);
								PlotManager.removeSellSign(w, PlotID);

								SqlManager.deletePlot(PlotManager.getIdX(PlotID), PlotManager.getIdZ(PlotID), w.getName().toLowerCase());
							}
						};
						SchedulerUtils.callSyncTaskAndWait(delPlot);

						delplots +=1;
					}
				}
			}
		}

		MessageLogger.debug("PlotMe purge finished, deleted "+ delplots +" inactive plots");
	}

}
