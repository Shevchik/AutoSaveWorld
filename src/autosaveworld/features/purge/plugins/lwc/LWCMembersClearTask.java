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

import java.util.LinkedList;

import com.griefcraft.model.Permission;
import com.griefcraft.model.Protection;

import autosaveworld.features.purge.taskqueue.Task;

public class LWCMembersClearTask implements Task {

	private Protection protection;

	public LWCMembersClearTask(Protection protection) {
		this.protection = protection;
	}

	private LinkedList<Permission> perms = new LinkedList<Permission>();

	public void add(Permission permission) {
		perms.add(permission);
	}

	public boolean hasPlayersToClear() {
		return !perms.isEmpty();
	}

	public int getPlayerToClearCount() {
		return perms.size();
	}

	@Override
	public boolean doNotQueue() {
		return false;
	}

	@Override
	public void performTask() {
		for (Permission permission : perms) {
			protection.removePermissions(permission.getName(), permission.getType());
		}
	}

}
