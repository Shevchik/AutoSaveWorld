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
import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.World;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import autosaveworld.core.AutoSaveWorld;

public class VaultPurge {
	
	private AutoSaveWorld plugin;
	
	public VaultPurge(AutoSaveWorld plugin)
	{
		this.plugin = plugin;
	}
	
	
	public void doPermissionsPurgeTask(ActivePlayersList pacheck)
	{
		Permission permission = Bukkit.getServicesManager().getRegistration(Permission.class).getProvider();
		int deleted = 0;
		String worldfoldername = Bukkit.getWorlds().get(0).getWorldFolder().getAbsolutePath();
		File playersdatfolder = new File(worldfoldername+ File.separator + "players"+ File.separator);
		for (String playerfile : playersdatfolder.list()) 
		{
			String playername = playerfile.substring(0, playerfile.indexOf("."));
			if (!pacheck.isActiveCS(playername)) 
			{
				plugin.debug(playername + " is inactive. Removing permissions");
				for (String group : new ArrayList<String>(Arrays.asList(permission.getPlayerGroups((World)null, playername))))
				{
					permission.playerRemoveGroup((World) null, playername, group);
				}
				deleted += 1;
			}
		}

		plugin.debug("Player permissions purge finished, deleted "+deleted+" player permissions");
	}
	
	public void doEconomyPurgeTask(ActivePlayersList pacheck)
	{
		Economy economy = Bukkit.getServicesManager().getRegistration(Economy.class).getProvider();
		int deleted = 0;
		String worldfoldername = Bukkit.getWorlds().get(0).getWorldFolder().getAbsolutePath();
		File playersdatfolder = new File(worldfoldername+ File.separator + "players"+ File.separator);
		for (String playerfile : playersdatfolder.list()) 
		{
			String playername = playerfile.substring(0, playerfile.indexOf("."));
			if (!pacheck.isActiveCS(playername)) 
			{
				plugin.debug(playername + " is inactive. Removing economy account");
				economy.withdrawPlayer(playername, economy.getBalance(playername));
				deleted += 1;
			}
		}

		plugin.debug("Player economy purge finished, deleted "+deleted+" player economy account");
	}

}
