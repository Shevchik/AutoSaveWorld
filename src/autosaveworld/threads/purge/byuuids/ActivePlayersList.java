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

package autosaveworld.threads.purge.byuuids;

import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import autosaveworld.config.AutoSaveWorldConfig;
import autosaveworld.core.logging.MessageLogger;

public class ActivePlayersList {

	private AutoSaveWorldConfig config;
	public ActivePlayersList(AutoSaveWorldConfig config) {
		this.config = config;
	}

	private HashSet<String> plactiveUUID = new HashSet<String>();

	//TODO: remove name storage as soon as all plugins update to UUID

	//storage to support checking by names
	private HashSet<String> plactiveNamesCS = new HashSet<String>();
	private HashSet<String> plactiveNamesNCS = new HashSet<String>();

	public void gatherActivePlayersList(long awaytime) {
		try {
			//fill list
			//add online players
			for (Player player : Bukkit.getOnlinePlayers()) {
				String uuidstring = player.getUniqueId().toString().replace("-", "");
				MessageLogger.debug("Adding online player "+uuidstring+" to active list");
				plactiveUUID.add(uuidstring);
				//old api storage
				if (config.purgeUseOldAPI) {
					plactiveNamesCS.add(player.getName());
					plactiveNamesNCS.add(player.getName().toLowerCase());
				}
			}
			OfflinePlayer[] offplayers = Bukkit.getOfflinePlayers();
			//add offline players that were away not for that long
			for (OfflinePlayer offplayer : offplayers) {
				String uuidstring = offplayer.getUniqueId().toString().replace("-", "");
				MessageLogger.debug("Checking player "+uuidstring);
				if (System.currentTimeMillis() - offplayer .getLastPlayed() < awaytime) {
					MessageLogger.debug("Adding player "+uuidstring+" to active list");
					plactiveUUID.add(uuidstring);
					//old api storage
					if (config.purgeUseOldAPI) {
						String name = offplayer.getName();
						if (name != null) {
							plactiveNamesCS.add(name);
							plactiveNamesNCS.add(name.toLowerCase());
						}
					}
				}
			}
			//add players from ignored list
			for (String name : config.purgeIgnoredNicks) {
				for (OfflinePlayer offplayer : offplayers) {
					if (offplayer.getName() != null && offplayer.getName().equalsIgnoreCase(name)) {
						String uuidstring = offplayer.getUniqueId().toString().replace("-", "");
						MessageLogger.debug("Adding ignored player "+uuidstring+" to active list");
						config.purgeIgnoredUUIDs.add(uuidstring);
					}
				}
			}
			config.purgeIgnoredNicks.clear();
			for (String listuuid : config.purgeIgnoredUUIDs) {
				MessageLogger.debug("Adding ignored player "+listuuid.replace("-", "")+" to active list");
				plactiveUUID.add(listuuid.replace("-", ""));
				//old api storage
				if (config.purgeUseOldAPI) {
					OfflinePlayer offplayer = Bukkit.getOfflinePlayer(UUID.fromString(listuuid));
					String name = offplayer.getName();
					if (name != null) {
						plactiveNamesCS.add(name);
						plactiveNamesNCS.add(name.toLowerCase());
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to gather active players list");
		}
	}

	public int getActivePlayersCount() {
		return plactiveUUID.size();
	}

	public boolean isActiveUUID(String uuid) {
		uuid = uuid.replace("-", "");
		return plactiveUUID.contains(uuid);
	}

	//old api

	public boolean isActiveNameCS(String name) {
		return plactiveNamesCS.contains(name);
	}

	public boolean isActiveNameNCS(String name) {
		return plactiveNamesNCS.contains(name.toLowerCase());
	}

}
