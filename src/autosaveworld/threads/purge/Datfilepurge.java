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
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import autosaveworld.core.AutoSaveWorld;

public class Datfilepurge {

	private AutoSaveWorld plugin;

	public Datfilepurge(AutoSaveWorld plugin)
	{
		this.plugin = plugin;
	}
	
	public void doDelPlayerDatFileTask(PlayerActiveCheck pacheck) {
		int deleted = 0;
		OfflinePlayer[] checkPlayers = Bukkit.getServer().getOfflinePlayers();

		try {
			String worldfoldername = Bukkit.getWorlds().get(0).getWorldFolder().getCanonicalPath();
			for (OfflinePlayer pl : checkPlayers) 
			{
				if (!pacheck.isActiveCS(pl.getName())) 
				{
							File pldatFile = new File(
											worldfoldername
											+ File.separator + "players"
											+ File.separator + pl.getName()
											+ ".dat");
							pldatFile.delete();
							plugin.debug(pl.getName() + " is inactive. Removing dat file");
							deleted += 1;
				}
			}
		} catch (Exception e) {}
		
		plugin.debug("Player .dat purge finished, deleted "+deleted+" player .dat files");
		
	}

}
