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



	private ArrayList<String> playerstopurgeperms = new ArrayList<String>(70);
	public void doPermissionsPurgeTask(ActivePlayersList pacheck)
	{
		Permission permission = Bukkit.getServicesManager().getRegistration(Permission.class).getProvider();
		int deleted = 0;
		String worldfoldername = Bukkit.getWorlds().get(0).getWorldFolder().getAbsolutePath();
		File playersdatfolder = new File(worldfoldername+ File.separator + "players"+ File.separator);
		for (String playerfile : playersdatfolder.list())
		{
			if (playerfile.endsWith(".dat"))
			{
				String playername = playerfile.substring(0, playerfile.length() - 4);
				if (!pacheck.isActiveCS(playername))
				{
					//add player to delete batch
					playerstopurgeperms.add(playername);
					//delete permissions if maximum batch size reached
					if (playerstopurgeperms.size() == 40)
					{
						flushPermsBatch(permission);
					}
					deleted += 1;
				}
			}
		}
		//flush the rest of the batch
		flushPermsBatch(permission);

		plugin.debug("Player permissions purge finished, deleted "+deleted+" players permissions");
	}
	private void flushPermsBatch(final Permission permission)
	{
		//detete permissions
		Runnable deleteperms = new Runnable()
		{
			@Override
			public void run()
			{
				for (String playername : playerstopurgeperms)
				{
					plugin.debug(playername + " is inactive. Removing permissions");
					//remove all player groups
					for (String group : permission.getGroups())
					{
						permission.playerRemoveGroup((String)null, playername, group);
						for (World world : Bukkit.getWorlds())
						{
							permission.playerRemoveGroup(world, playername, group);
						}
					}
				}
				playerstopurgeperms.clear();
			}
		};
		int taskid = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, deleteperms);

		//Wait until previous permissions delete is finished to avoid full main thread freezing
		while (Bukkit.getScheduler().isCurrentlyRunning(taskid) || Bukkit.getScheduler().isQueued(taskid))
		{
			try {Thread.sleep(100);} catch (InterruptedException e) {}
		}
	}

	private ArrayList<String> playerstopurgeecon = new ArrayList<String>(70);
	public void doEconomyPurgeTask(ActivePlayersList pacheck)
	{
		Economy economy = Bukkit.getServicesManager().getRegistration(Economy.class).getProvider();
		int deleted = 0;
		String worldfoldername = Bukkit.getWorlds().get(0).getWorldFolder().getAbsolutePath();
		File playersdatfolder = new File(worldfoldername+ File.separator + "players"+ File.separator);
		for (String playerfile : playersdatfolder.list())
		{
			if (playerfile.endsWith(".dat"))
			{
				String playername = playerfile.substring(0, playerfile.length() - 4);
				if (!pacheck.isActiveCS(playername))
				{
					//add player to delete batch
					playerstopurgeecon.add(playername);
					//delete economy if maximum batch size reached
					if (playerstopurgeecon.size() == 40)
					{
						flushEconomyBatch(economy);
					}
					deleted += 1;
				}
			}
		}
		//flush the rest of the batch
		flushEconomyBatch(economy);

		plugin.debug("Player economy purge finished, deleted "+deleted+" players economy accounts");
	}
	private void flushEconomyBatch(final Economy economy)
	{
		//detete permissions
		Runnable deleteeconomy = new Runnable()
		{
			@Override
			public void run()
			{
				for (String playername : playerstopurgeecon)
				{
					plugin.debug(playername + " is inactive. Removing economy account");
					//remove all player groups
					economy.withdrawPlayer(playername, economy.getBalance(playername));
					economy.deleteBank(playername);
				}
				playerstopurgeecon.clear();
			}
		};
		int taskid = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, deleteeconomy);

		//Wait until previous permissions delete is finished to avoid full main thread freezing
		while (Bukkit.getScheduler().isCurrentlyRunning(taskid) || Bukkit.getScheduler().isQueued(taskid))
		{
			try {Thread.sleep(100);} catch (InterruptedException e) {}
		}
	}

}
