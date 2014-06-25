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

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.UUID;

import net.minecraft.util.com.mojang.authlib.GameProfile;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
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
		try {
			//fill list
			//add online players
			for (Player player : Bukkit.getOnlinePlayers()) {
				String uuidstring = player.getUniqueId().toString().replace("-", "");
				MessageLogger.debug("Adding online player "+uuidstring+" to active list");
				plactive.add(uuidstring);
			}
			//add offline players that were away not for that long
			//getOfflinePlayer caches the offline player instance and we don't wan't it so we have to construct it manually
			Server server = Bukkit.getServer();
			Class<?> craftserver = server.getClass();
			Class<?> craftofflineplayer = Bukkit.getOfflinePlayer(UUID.randomUUID()).getClass();
			Constructor<?> ctor = craftofflineplayer.getDeclaredConstructor(craftserver, net.minecraft.util.com.mojang.authlib.GameProfile.class);
			ctor.setAccessible(true);
			File playersdir = new File(Bukkit.getWorlds().get(0).getWorldFolder(), "playerdata");
			for (String file : playersdir.list()) {
				if (file.endsWith(".dat")) {
					UUID uuid = UUID.fromString(file.substring(0, file.length() - 4));
					String uuidstring = uuid.toString().replace("-", "");
					MessageLogger.debug("Checking player "+uuidstring);
					OfflinePlayer offplayer = (OfflinePlayer) ctor.newInstance(server, new GameProfile(uuid, null));
					if (System.currentTimeMillis() - offplayer .getLastPlayed() < awaytime) {
						MessageLogger.debug("Adding player "+uuidstring+" to active list");
						plactive.add(uuidstring);
					}
				}
			}
			//add players from ignored list
			for (String name : config.purgeIgnoredNicks) {
				try {
					String uuidstring = Bukkit.getOfflinePlayer(name).getUniqueId().toString().replace("-", "");
					MessageLogger.debug("Adding ignored player "+uuidstring+" to active list");
					config.purgeIgnoredUUIDs.add(uuidstring);
				} catch (Exception e) {
				}
			}
			config.purgeIgnoredNicks.clear();
			for (String listuuid : config.purgeIgnoredUUIDs) {
				MessageLogger.debug("Adding ignored player "+listuuid.replace("-", "")+" to active list");
				plactive.add(listuuid.replace("-", ""));
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to gather active players list");
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
