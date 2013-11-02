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
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class PlayerActiveCheck {

	private HashSet<String> plactivencs = new HashSet<String>();
	private HashSet<String> plactivecs = new HashSet<String>();

	public void gatherActivePlayersList(long awaytime)
	{
		//due to some strange bug getOfflinePlayers don't get all the players from their .dat files, some are missing
		HashSet<String> foundplayers = new HashSet<String>();
		//fill lists
		for (Player player : Bukkit.getOnlinePlayers()) {
			plactivecs.add(player.getName());
			plactivencs.add(player.getName().toLowerCase());
			foundplayers.add(player.getName());
		}
		for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
			if (System.currentTimeMillis() - player.getLastPlayed() < awaytime) {
				plactivecs.add(player.getName());
				plactivencs.add(player.getName().toLowerCase());
				foundplayers.add(player.getName());
			}
		}
		//add players that wasn't in getOfflinePlayers array but has their dat files
		String worldfoldername = Bukkit.getWorlds().get(0).getWorldFolder().getAbsolutePath();
		File playersdatfolder = new File(worldfoldername+ File.separator + "players"+ File.separator);
		for (File playerfile : playersdatfolder.listFiles()) 
		{
			String playername = playerfile.getName().substring(0, playerfile.getName().indexOf("."));
			if (!foundplayers.contains(playername))
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
