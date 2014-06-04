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

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;

import autosaveworld.core.logging.MessageLogger;
import autosaveworld.threads.purge.bynames.ActivePlayersList;
import autosaveworld.utils.SchedulerUtils;

import com.griefcraft.lwc.LWCPlugin;
import com.griefcraft.model.Protection;

public class LWCPurge {

	public void doLWCPurgeTask(ActivePlayersList pacheck, boolean delblocks) {

		MessageLogger.debug("LWC purge started");

		LWCPlugin lwc = (LWCPlugin) Bukkit.getPluginManager().getPlugin("LWC");

		int deleted = 0;

		//we will check LWC database and remove protections that belongs to away player
		for (final Protection pr : lwc.getLWC().getPhysicalDatabase().loadProtections()) {
			if (!pacheck.isActiveCS(pr.getOwner())) {
				//add protected to delete batch
				prtodel.add(pr);
				//delete protections if maximum batch size reached
				if (prtodel.size() == 80) {
					flushBatch(lwc, delblocks);
				}
				//count deleted protections
				deleted += 1;
			}
		}
		//flush the rest of the batch;
		flushBatch(lwc, delblocks);

		MessageLogger.debug("LWC purge finished, deleted "+ deleted+" inactive protections");
	}

	private ArrayList<Protection> prtodel = new ArrayList<Protection>(100);
	private void flushBatch(final LWCPlugin lwc, final boolean delblocks) {
		Runnable rempr = new Runnable() {
			@Override
			public void run() {
				for (Protection pr : prtodel) {
					//delete block
					if (delblocks) {
						MessageLogger.debug("Removing protected block for inactive player "+pr.getOwner());
						Block block = pr.getBlock();
						BlockState bs = block.getState();
						if (bs instanceof Chest) {
							((Chest) bs).getBlockInventory().clear();
						} else
						if (bs instanceof DoubleChest) {
							((DoubleChest) bs).getInventory().clear();
						}
						block.setType(Material.AIR);
					}
					//delete protection
					MessageLogger.debug("Removing protection for inactive player "+pr.getOwner());
					lwc.getLWC().getPhysicalDatabase().removeProtection(pr.getId());
				}
				prtodel.clear();
			}
		};
		SchedulerUtils.callSyncTaskAndWait(rempr);
	}

}
