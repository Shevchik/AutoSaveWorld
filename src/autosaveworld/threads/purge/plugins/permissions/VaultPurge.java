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

package autosaveworld.threads.purge.plugins.permissions;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import autosaveworld.core.logging.MessageLogger;
import autosaveworld.threads.purge.ActivePlayersList;
import autosaveworld.utils.SchedulerUtils;

public class VaultPurge {

	public void doPurge(ActivePlayersList activePlayersStorage) {

		int deleted = 0;

		Permission permission = Bukkit.getServicesManager().getRegistration(Permission.class).getProvider();
		TaskQueue queue = new TaskQueue(permission);

		String worldfoldername = Bukkit.getWorlds().get(0).getWorldFolder().getAbsolutePath();
		File playersdatfolder = new File(worldfoldername + File.separator + "playerdata" + File.separator);
		for (String playerfile : playersdatfolder.list()) {
			if (playerfile.endsWith(".dat")) {
				String playerUUID = playerfile.substring(0, playerfile.length() - 4);
				if (!activePlayersStorage.isActiveUUID(playerUUID)) {
					//delete player permissions
					queue.add(playerUUID);
					deleted += 1;
				}
			}
		}
		// flush the rest of the queue
		queue.flush();

		MessageLogger.debug("Player permissions purge finished, deleted " + deleted + " players permissions");
	}

	private class TaskQueue {

		private Permission permission;

		public TaskQueue(Permission permission) {
			this.permission = permission;
		}

		private ArrayList<String> playerstopurge = new ArrayList<String>(70);

		public void add(String player) {
			playerstopurge.add(player);
			if (playerstopurge.size() == 40) {
				flush();
			}
		}

		public void flush() {
			Runnable deleteperms = new Runnable() {
				@Override
				public void run() {
					for (String playerUUID : playerstopurge) {
						MessageLogger.debug(playerUUID + " is inactive. Removing permissions");
						OfflinePlayer offpl = Bukkit.getOfflinePlayer(UUID.fromString(playerUUID));
						// remove all player groups
						for (String group : permission.getGroups()) {
							permission.playerRemoveGroup((String) null, offpl, group);
							for (World world : Bukkit.getWorlds()) {
								permission.playerRemoveGroup(world.getName(), offpl, group);
							}
						}
					}
					playerstopurge.clear();
				}
			};
			SchedulerUtils.callSyncTaskAndWait(deleteperms);
		}

	}

}
