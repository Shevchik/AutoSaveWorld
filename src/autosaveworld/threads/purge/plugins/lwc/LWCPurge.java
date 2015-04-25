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

package autosaveworld.threads.purge.plugins.lwc;

import java.util.List;

import autosaveworld.config.AutoSaveWorldConfig;
import autosaveworld.core.logging.MessageLogger;
import autosaveworld.threads.purge.ActivePlayersList;
import autosaveworld.threads.purge.DataPurge;
import autosaveworld.threads.purge.taskqueue.TaskQueue;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Permission;
import com.griefcraft.model.Protection;
import com.griefcraft.model.Protection.Type;

public class LWCPurge extends DataPurge {

	public LWCPurge(AutoSaveWorldConfig config, ActivePlayersList activeplayerslist) {
		super(config, activeplayerslist);
	}

	public void doPurge() {

		MessageLogger.debug("LWC purge started");

		int deleted = 0;

		TaskQueue queue = new TaskQueue(30);
		List<Protection> protections = LWC.getInstance().getPhysicalDatabase().loadProtections();
		for (final Protection pr : protections) {
			LWCMembersClearTask clearTask = new LWCMembersClearTask(pr);
			if (pr.getType() == Type.PRIVATE) {
				for (Permission permission : pr.getPermissions()) {
					if (!activeplayerslist.isActiveName(permission.getName()) && !activeplayerslist.isActiveUUID(permission.getName())) {
						clearTask.add(permission);
					}
				}
			}
			if (!activeplayerslist.isActiveName(pr.getOwner()) && !activeplayerslist.isActiveUUID(pr.getOwner()) && (clearTask.getPlayerToClearCount() == pr.getPermissions().size())) {
				MessageLogger.debug("Protection owner "+pr.getOwner()+" is inactive");
				// regen block if needed
				if (config.purgeLWCDelProtectedBlocks) {
					LWCRegenTask regenTask = new LWCRegenTask(pr);
					queue.addTask(regenTask);
				}
				// delete protection
				LWCDeleteTask deleteTask = new LWCDeleteTask(pr);
				queue.addTask(deleteTask);

				deleted++;
			}
			// cleanup protection members if needed
			if (clearTask.hasPlayersToClear()) {
				queue.addTask(clearTask);
			}
		}
		// flush the rest of the queue
		queue.flush();

		MessageLogger.debug("LWC purge finished, deleted " + deleted + " inactive protections");
	}

}
