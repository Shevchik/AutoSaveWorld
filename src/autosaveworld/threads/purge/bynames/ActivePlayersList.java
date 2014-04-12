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

package autosaveworld.threads.purge.bynames;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;

import autosaveworld.config.AutoSaveWorldConfig;
import autosaveworld.core.AutoSaveWorld;

public class ActivePlayersList {

	private AutoSaveWorld plugin;
	private AutoSaveWorldConfig config;
	public ActivePlayersList(AutoSaveWorld plugin, AutoSaveWorldConfig config) {
		this.plugin = plugin;
		this.config = config;
	}

	private HashSet<String> plactivencs = new HashSet<String>();
	private HashSet<String> plactivecs = new HashSet<String>();

	public void gatherActivePlayersList(long awaytime) {
		try {
			//fill lists
			//due to bukkit fucks up itself when we have two player files with different case (test.dat and Test.dat), i had to write this...
			Server server = Bukkit.getServer();
			Class<?> craftofflineplayer = Bukkit.getOfflinePlayer("fakeautopurgeplayer").getClass();
			Constructor<?> ctor = craftofflineplayer.getDeclaredConstructor(server.getClass(),String.class);
			ctor.setAccessible(true);
			File playersdir = new File(Bukkit.getWorlds().get(0).getWorldFolder(),"players");
			for (String file : playersdir.list()) {
				if (file.endsWith(".dat")) {
					String nickname = file.substring(0, file.length() - 4);
					plugin.debug("Checking player "+nickname);
					OfflinePlayer offplayer = (OfflinePlayer) ctor.newInstance(server,nickname);
					if (System.currentTimeMillis() - offplayer.getLastPlayed() < awaytime) {
						System.out.println("Adding player "+nickname+" to active list");
						plactivecs.add(offplayer.getName());
						plactivencs.add(offplayer.getName().toLowerCase());
					}
				}
			}
			for (String ignorednick : config.purgeIgnoredNicks) {
				plactivecs.add(ignorednick);
				plactivencs.add(ignorednick.toLowerCase());
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to gather active players list");
		}
	}

	public int getActivePlayersCount() {
		return plactivecs.size();
	}

	public boolean isActiveNCS(String playername) {
		return plactivencs.contains(playername.toLowerCase());
	}

	public boolean isActiveCS(String playername) {
		return plactivecs.contains(playername);
	}


}
