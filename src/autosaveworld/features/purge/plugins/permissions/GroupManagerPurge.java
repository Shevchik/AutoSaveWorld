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

package autosaveworld.features.purge.plugins.permissions;

import java.util.ArrayList;
import java.util.Map;

import org.anjocaido.groupmanager.GroupManager;
import org.anjocaido.groupmanager.data.User;
import org.anjocaido.groupmanager.dataholder.OverloadedWorldHolder;
import org.bukkit.plugin.java.JavaPlugin;

import autosaveworld.core.logging.MessageLogger;
import autosaveworld.features.purge.ActivePlayersList;
import autosaveworld.features.purge.DataPurge;

public class GroupManagerPurge extends DataPurge {

	public GroupManagerPurge(ActivePlayersList activeplayerslist) {
		super("Permissions(GroupManager)", activeplayerslist);
	}

	public void doPurge() {
		GroupManager groupManager = JavaPlugin.getPlugin(GroupManager.class);
		for (OverloadedWorldHolder holder : groupManager.getWorldsHolder().allWorldsDataList()) {
			Map<String, User> users = holder.getUsers();
			for (User user : new ArrayList<User>(users.values())) {
				String userid = user.getUUID();
				if (!activeplayerslist.isActiveName(userid) && !activeplayerslist.isActiveUUID(userid)) {
					MessageLogger.debug("Player "+userid+" is inactive. Removing permissions");
					holder.removeUser(userid);
					incDeleted();
				}
			}
		}
	}

}
