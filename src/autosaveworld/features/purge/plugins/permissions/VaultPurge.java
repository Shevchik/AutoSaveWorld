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

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import autosaveworld.config.AutoSaveWorldConfig;
import autosaveworld.core.logging.MessageLogger;
import autosaveworld.features.purge.ActivePlayersList;
import autosaveworld.features.purge.DataPurge;
import autosaveworld.utils.FileUtils;
import autosaveworld.utils.SchedulerUtils;
import net.milkbowl.vault.permission.Permission;

public class VaultPurge extends DataPurge {

	public VaultPurge(AutoSaveWorldConfig config, ActivePlayersList activeplayerslist) {
		super(config, activeplayerslist);
	}

	public void doPurge() {
		MessageLogger.debug("Player permissions purge started");

		Permission permission = Bukkit.getServicesManager().getRegistration(Permission.class).getProvider();
		TaskQueue queue = new TaskQueue(permission);

		String worldfoldername = Bukkit.getWorlds().get(0).getWorldFolder().getAbsolutePath();
		File playersdatfolder = new File(worldfoldername + File.separator + "playerdata" + File.separator);
		for (File playerfile : FileUtils.safeListFiles(playersdatfolder)) {
			String playerfilename = playerfile.getName();
			if (playerfilename.endsWith(".dat")) {
				String playerUUID = playerfilename.substring(0, playerfilename.length() - 4);
				if (!activeplayerslist.isActiveUUID(playerUUID)) {
					//delete player permissions
					queue.add(playerUUID);
					incDeleted();
				}
			}
		}
		// flush the rest of the queue
		queue.flush();

		MessageLogger.debug("Player permissions purge finished, deleted " + getDeleted() + " players permissions");
	}

	private static class TaskQueue {

		protected Permission permission;

		public TaskQueue(Permission permission) {
			this.permission = permission;
		}

		protected ArrayList<String> playerstopurge = new ArrayList<String>(70);

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
