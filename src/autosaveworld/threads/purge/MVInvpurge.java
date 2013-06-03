package autosaveworld.threads.purge;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.api.profile.WorldGroupProfile;

import autosaveworld.core.AutoSaveWorld;

public class MVInvpurge {

	private AutoSaveWorld plugin;

	public MVInvpurge(AutoSaveWorld plugin, long awaytime)
	{
		this.plugin = plugin;
		MVInvPurgeTask(awaytime);
	}
	
	
	private void MVInvPurgeTask(long awaytime)
	{
		try {
		MultiverseInventories mvpl = (MultiverseInventories) Bukkit.getPluginManager().getPlugin("Multiverse-Inventories");
		File mcinvpfld = new File("plugins/Multiverse-Inventories/players/");
		int deleted = 0;
		//We will get all files from MVInv player directory, and get player names from there
		for (String plfile : mcinvpfld.list())
		{
			String plname = plfile.substring(0, plfile.indexOf("."));
				boolean remove = false;
				OfflinePlayer offpl = (Bukkit.getOfflinePlayer(plname));
				if (!offpl.hasPlayedBefore()) {remove = true;}
				else if (System.currentTimeMillis() - offpl.getLastPlayed() >= awaytime) {remove = true;}
				if (remove) {
					plugin.debug("Removing "+plname+" MVInv files");
					//remove files from MVInv world folders
					for (World wname : Bukkit.getWorlds()) {
						mvpl.getWorldManager().getWorldProfile(wname.getName()).removeAllPlayerData(offpl);
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
		
		plugin.debug("MVInv purge finished, deleted "+deleted+" player files, Warning: on some Multiverse-Inventories versions you should divide this number by 2 to know the real count");
		
		} catch (Exception e) {e.printStackTrace();}
	}
	
	
}
