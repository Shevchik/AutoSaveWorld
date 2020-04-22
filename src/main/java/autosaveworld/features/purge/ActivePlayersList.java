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

package autosaveworld.features.purge;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import autosaveworld.core.logging.MessageLogger;
import autosaveworld.utils.BukkitUtils;

public class ActivePlayersList {

	private final List<String> ignoreNicks;
	private final List<String> ignoreUUIDs;
	public ActivePlayersList(List<String> ignoreNicks, List<String> ignoreUUIDs) {
		this.ignoreNicks = ignoreNicks;
		this.ignoreUUIDs = ignoreUUIDs;
	}

	private final HashSet<String> plactiveUUID = new HashSet<String>();
	private final HashSet<String> plactiveNames = new HashSet<String>();

	private OfflinePlayer[] players;

	public void calculateActivePlayers(long awaytime) {
		// fill lists
		// add online players
		for (Player player : BukkitUtils.getOnlinePlayers()) {
			String uuidstring = player.getUniqueId().toString().replace("-", "");
			MessageLogger.debug("Adding online player " + uuidstring + " to active list");
			plactiveUUID.add(uuidstring);
			plactiveNames.add(player.getName().toLowerCase());
		}
		players = Bukkit.getOfflinePlayers();
		// add offline players that were away not for that long
		for (OfflinePlayer offplayer : players) {
			String uuidstring = offplayer.getUniqueId().toString().replace("-", "");
			MessageLogger.debug("Checking player " + uuidstring);
			if ((System.currentTimeMillis() - offplayer.getLastPlayed()) < awaytime) {
				MessageLogger.debug("Adding player " + uuidstring + " to active list");
				plactiveUUID.add(uuidstring);
				String name = offplayer.getName();
				if (name != null) {
					plactiveNames.add(name.toLowerCase());
				}
			}
		}
		// add players from ignored lists
		for (OfflinePlayer offplayer : players) {
			if ((offplayer.getName() != null) && ignoreNicks.contains(offplayer.getName())) {
				String uuidstring = offplayer.getUniqueId().toString();
				MessageLogger.debug("Adding ignored player " + uuidstring.replace("-", "") + " to active list");
				ignoreUUIDs.add(uuidstring);
			}
		}
		for (String listuuid : ignoreUUIDs) {
			MessageLogger.debug("Adding ignored player " + listuuid.replace("-", "") + " to active list");
			plactiveUUID.add(listuuid.replace("-", ""));
			OfflinePlayer offplayer = Bukkit.getOfflinePlayer(UUID.fromString(listuuid));
			String name = offplayer.getName();
			if (name != null) {
				plactiveNames.add(name.toLowerCase());
			}
		}
	}

	public OfflinePlayer[] getAllPlayers() {
		return players;
	}

	public int getActivePlayersCount() {
		return plactiveUUID.size();
	}

	public boolean isActiveUUID(UUID uuid) {
		if (uuid == null) {
			return true;
		}
		return isActiveUUID(uuid.toString());
	}

	public boolean isActiveUUID(String uuid) {
		if (uuid == null) {
			return true;
		}
		uuid = uuid.replace("-", "");
		return plactiveUUID.contains(uuid);
	}

	public boolean isActiveName(String name) {
		if (name == null) {
			return true;
		}
		return plactiveNames.contains(name.toLowerCase());
	}

}
