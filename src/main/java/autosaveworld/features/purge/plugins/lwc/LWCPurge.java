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

package autosaveworld.features.purge.plugins.lwc;

import java.util.List;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Permission;
import com.griefcraft.model.Protection;

import autosaveworld.core.AutoSaveWorld;
import autosaveworld.core.logging.MessageLogger;
import autosaveworld.features.purge.ActivePlayersList;
import autosaveworld.features.purge.DataPurge;
import autosaveworld.features.purge.taskqueue.TaskExecutor;

public class LWCPurge extends DataPurge {

	public LWCPurge(ActivePlayersList activeplayerslist) {
		super("LWC", activeplayerslist);
	}

	@Override
	public void doPurge() {
		try (TaskExecutor queue = new TaskExecutor(30)) {
			List<Protection> protections = LWC.getInstance().getPhysicalDatabase().loadProtections();
			for (final Protection pr : protections) {
				LWCMembersClearTask clearTask = new LWCMembersClearTask(pr);
				for (Permission permission : pr.getPermissions()) {
					if (!activeplayerslist.isActiveName(permission.getName()) && !activeplayerslist.isActiveUUID(permission.getName())) {
						clearTask.add(permission);
					}
				}
				if (!activeplayerslist.isActiveName(pr.getOwner()) && !activeplayerslist.isActiveUUID(pr.getOwner()) && (clearTask.getPlayerToClearCount() == pr.getPermissions().size())) {
					MessageLogger.debug("Protection owner "+pr.getOwner()+" is inactive");
					// regen block if needed
					if (AutoSaveWorld.getInstance().getMainConfig().purgeLWCDelProtectedBlocks) {
						LWCRegenTask regenTask = new LWCRegenTask(pr);
						queue.execute(regenTask);
					}
					// delete protection
					LWCDeleteTask deleteTask = new LWCDeleteTask(pr);
					queue.execute(deleteTask);
					incDeleted();
					continue;
				}
				// cleanup protection members if needed
				if (clearTask.hasPlayersToClear()) {
					queue.execute(clearTask);
					incCleaned();
				}
			}
		}
	}

}
