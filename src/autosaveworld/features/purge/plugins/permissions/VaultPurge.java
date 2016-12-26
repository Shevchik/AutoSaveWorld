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

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import autosaveworld.features.purge.ActivePlayersList;
import autosaveworld.features.purge.DataPurge;
import autosaveworld.features.purge.taskqueue.Task;
import autosaveworld.features.purge.taskqueue.TaskExecutor;
import net.milkbowl.vault.permission.Permission;

public class VaultPurge extends DataPurge {

	public VaultPurge(ActivePlayersList activeplayerslist) {
		super("Permissions(Vault)", activeplayerslist);
	}

	public void doPurge() {
		final Permission permission = Bukkit.getServicesManager().getRegistration(Permission.class).getProvider();

		try (TaskExecutor queue = new TaskExecutor(80)) {
			for (final OfflinePlayer player : activeplayerslist.getAllPlayers()) {
				if (!activeplayerslist.isActiveUUID(player.getUniqueId())) {
					queue.execute(new Task() {
						@Override
						public boolean doNotQueue() {
							return false;
						}
						@Override
						public void performTask() {
							for (String group : permission.getGroups()) {
								permission.playerRemoveGroup((String) null, player, group);
								for (World world : Bukkit.getWorlds()) {
									permission.playerRemoveGroup(world.getName(), player, group);
								}
							}
						}
					});
					incDeleted();
				}
			}
		}
	}

}
