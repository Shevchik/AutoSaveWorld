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

	private HashSet<String> plactive = new HashSet<String>();

	@SuppressWarnings("deprecation")
	public void gatherActivePlayersList(long awaytime) {
		//fill list
		//add online players
		for (Player player : Bukkit.getOnlinePlayers()) {
			plactive.add(player.getUniqueId().toString().replace("-", ""));
		}
		//add offline players that were away not for that long
		for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
			String uuid = player.getUniqueId().toString().replace("-", "");
			MessageLogger.debug("Checking player "+uuid);
			if (System.currentTimeMillis() - player.getLastPlayed() < awaytime) {
				MessageLogger.debug("Adding player "+uuid+" to active list");
				plactive.add(uuid);
			}
		}
		//add players from ignored list
		for (String name : config.purgeIgnoredNicks) {
			try {
				config.purgeIgnoredUUIDs.add(Bukkit.getOfflinePlayer(name).getUniqueId().toString().replace("-", ""));
			} catch (Exception e) {
			}
		}
		config.purgeIgnoredNicks.clear();
		for (String listuuid : config.purgeIgnoredUUIDs) {
			plactive.add(listuuid.replace("-", ""));
		}
	}

	public int getActivePlayersCount() {
		return plactive.size();
	}

	public boolean isActive(String uuid) {
		uuid = uuid.replace("-", "");
		return plactive.contains(uuid);
	}

}
