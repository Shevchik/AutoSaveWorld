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

package autosaveworld.threads.purge;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class PlayerActiveCheck {

	private HashSet<String> plactivencs = new HashSet<String>();
	private HashSet<String> plactivecs = new HashSet<String>();

	public void gatherActivePlayersList(long awaytime)
	{
		//fill lists
		for (Player plname : Bukkit.getOnlinePlayers()) {
			plactivecs.add(plname.getName());
			plactivencs.add(plname.getName().toLowerCase());
		}
		HashSet<OfflinePlayer> offlinePlayers = new HashSet<OfflinePlayer>();
		offlinePlayers.addAll(Arrays.asList(Bukkit.getOfflinePlayers()));
		for (OfflinePlayer plname : Bukkit.getOfflinePlayers()) {
			if (System.currentTimeMillis() - plname.getLastPlayed() < awaytime) {
				plactivecs.add(plname.getName());
				plactivencs.add(plname.getName().toLowerCase());
			}
		}
		//for some reason getOfflinePlayers() misses some players so we will assume that they are active
		String worldfoldername = Bukkit.getWorlds().get(0).getWorldFolder().getAbsolutePath();
		File playersdatfolder = new File(worldfoldername+ File.separator + "players"+ File.separator);
		for (String playerfilename : playersdatfolder.list()) 
		{
			String playername = playerfilename.substring(0, playerfilename.indexOf("."));
			if (!offlinePlayers.contains(playername))
			{
				plactivecs.add(playername);
				plactivencs.add(playername.toLowerCase());
			}
		}
	}

	public boolean isActiveNCS(String playername)
	{
		return plactivencs.contains(playername);
	}

	public boolean isActiveCS(String playername)
	{
		return plactivecs.contains(playername);
	}
	
	
}
