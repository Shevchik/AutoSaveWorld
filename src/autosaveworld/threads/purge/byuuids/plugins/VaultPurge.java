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

package autosaveworld.threads.purge.byuuids.plugins;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import autosaveworld.core.logging.MessageLogger;
import autosaveworld.threads.purge.byuuids.ActivePlayersList;
import autosaveworld.utils.SchedulerUtils;

public class VaultPurge {

	private ArrayList<String> playerstopurgeperms = new ArrayList<String>(70);
	public void doPermissionsPurgeTask(ActivePlayersList pacheck, String savecmd) {

		MessageLogger.debug("Player permissions purge started");

		Permission permission = Bukkit.getServicesManager().getRegistration(Permission.class).getProvider();

		int deleted = 0;

		String worldfoldername = Bukkit.getWorlds().get(0).getWorldFolder().getAbsolutePath();
		File playersdatfolder = new File(worldfoldername+ File.separator + "playerdata"+ File.separator);
		for (String playerfile : playersdatfolder.list()) {
			if (playerfile.endsWith(".dat")) {
				String playerUUID = playerfile.substring(0, playerfile.length() - 4);
				if (!pacheck.isActiveUUID(playerUUID)) {
					//add player to delete batch
					playerstopurgeperms.add(playerUUID);
					//delete permissions if maximum batch size reached
					if (playerstopurgeperms.size() == 40) {
						flushPermsBatch(permission, savecmd);
					}
					deleted += 1;
				}
			}
		}
		//flush the rest of the batch
		flushPermsBatch(permission, savecmd);

		MessageLogger.debug("Player permissions purge finished, deleted "+deleted+" players permissions");
	}
	private void flushPermsBatch(final Permission permission, final String savecmd) {
		//delete permissions
		Runnable deleteperms = new Runnable() {
			@Override
			public void run() {
				for (String playerUUID : playerstopurgeperms) {
					MessageLogger.debug(playerUUID + " is inactive. Removing permissions");
					OfflinePlayer offpl = Bukkit.getOfflinePlayer(UUID.fromString(playerUUID));
					//remove all player groups
					for (String group : permission.getGroups()) {
						permission.playerRemoveGroup((String)null, offpl, group);
						for (World world : Bukkit.getWorlds()) {
							permission.playerRemoveGroup(world.getName(), offpl, group);
						}
					}
				}
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), savecmd);
				playerstopurgeperms.clear();
			}
		};
		SchedulerUtils.callSyncTaskAndWait(deleteperms);
	}

}
