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
import java.lang.reflect.Constructor;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;

import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.api.profile.WorldGroupProfile;

import autosaveworld.core.AutoSaveWorld;

public class MVInvPurge {

	private AutoSaveWorld plugin;

	public MVInvPurge(AutoSaveWorld plugin)
	{
		this.plugin = plugin;
	}


	public void doMVInvPurgeTask(ActivePlayersList pacheck)
	{
		int deleted = 0;
		try {
			MultiverseInventories mvpl = (MultiverseInventories) Bukkit.getPluginManager().getPlugin("Multiverse-Inventories");
			File mcinvpfld = new File("plugins/Multiverse-Inventories/players/");
			Server server = Bukkit.getServer();
			Class<?> craftofflineplayer = Bukkit.getOfflinePlayer("fakeautopurgeplayer").getClass();
			Constructor<?> ctor = craftofflineplayer.getDeclaredConstructor(server.getClass(),String.class);
			ctor.setAccessible(true);
			//We will get all files from MVInv player directory, and get player names from there
			for (String plfile : mcinvpfld.list())
			{
				String plname = plfile.substring(0, plfile.indexOf("."));

				if (!pacheck.isActiveCS(plname))
				{
					plugin.debug("Removing "+plname+" MVInv files");
					//remove files from MVInv world folders
					for (World wname : Bukkit.getWorlds())
					{
						mvpl.getWorldManager().getWorldProfile(wname.getName()).removeAllPlayerData((OfflinePlayer) ctor.newInstance(server,plname));
					}
					//remove files from MVInv player folder
					new File(mcinvpfld,plfile).delete();
					//remove files from MVInv groups folder
					for (WorldGroupProfile gname: mvpl.getGroupManager().getGroups())
					{
						File mcinvgfld = new File("plugins/Multiverse-Inventories/groups/");
						new File(mcinvgfld,gname.getName()+File.separator+plfile).delete();
					}
					//count deleted player file
					deleted += 1;
				}
			}
		} catch (Exception e) {}

		plugin.debug("MVInv purge finished, deleted "+deleted+" player files, Warning: on some Multiverse-Inventories versions you should divide this number by 2 to know the real count");
	}

}
