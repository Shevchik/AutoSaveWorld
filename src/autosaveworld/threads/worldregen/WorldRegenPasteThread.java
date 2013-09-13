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

package autosaveworld.threads.worldregen;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import autosaveworld.config.AutoSaveConfig;
import autosaveworld.config.AutoSaveConfigMSG;
import autosaveworld.core.AutoSaveWorld;
import autosaveworld.threads.worldregen.factions.FactionsPaste;
import autosaveworld.threads.worldregen.griefprevention.GPPaste;
import autosaveworld.threads.worldregen.wg.WorldGuardPaste;

public class WorldRegenPasteThread extends Thread {

	private AutoSaveWorld plugin = null;
	private AutoSaveConfig config;
	private AutoSaveConfigMSG configmsg;
	public WorldRegenPasteThread(AutoSaveWorld plugin, AutoSaveConfig config, AutoSaveConfigMSG configmsg) {
		this.plugin = plugin;
		this.config = config;
	};

	public long loaded = 0;
	private String worldtopasteto;
	public void run() {
		try {
			
			Thread.currentThread().setName("AutoSaveWorld WorldRegenPaste Thread");	
			
			//deny players from join
			AntiJoinListener ajl = new AntiJoinListener(plugin,configmsg);
			Bukkit.getPluginManager().registerEvents(ajl, plugin);
			
			//load config
			FileConfiguration cfg = YamlConfiguration.loadConfiguration(new File(plugin.constants.getWorldnameFile()));
			worldtopasteto = cfg.getString("wname");
			
			//wait for server to load
			int ltask = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
			{
				public void run()
				{
					loaded = System.currentTimeMillis();
				}
			});
			while (loaded == 0) {
				Thread.sleep(1000);
			}
			Bukkit.getScheduler().cancelTask(ltask);
			
			//check for worldedit
			if (Bukkit.getPluginManager().getPlugin("WorldEdit") == null) {
				plugin.broadcast("WorldEdit not found, can't place schematics back, please install WorldEdit and restart server");
				return;
			}
			
			plugin.debug("Restoring buildings");
			
			// paste WG buildings
			if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null && config.worldregensavewg) 
			{
				new WorldGuardPaste(plugin, worldtopasteto).pasteAllFromSchematics();
			}
			
			//paste Factions buildings
			if (Bukkit.getPluginManager().getPlugin("Factions") != null && config.worldregensavefactions) 
			{
				new FactionsPaste(plugin, worldtopasteto).pasteAllFromSchematics();
			}
			
			//paste GriefPrevention claims
			if (Bukkit.getPluginManager().getPlugin("GriefPrevention") != null && config.worldregensavegp)
			{
				new GPPaste(plugin,worldtopasteto).pasteAllFromSchematics();
			}

			//clear temp folder
			plugin.debug("Cleaning temp folders");
			
			deleteDirectory(new File(plugin.constants.getWGTempFolder()));
			deleteDirectory(new File(plugin.constants.getFactionsTempFolder()));
			deleteDirectory(new File(plugin.constants.getGPTempFolder()));
			
			plugin.debug("Restore finished");

			// restart
			plugin.worldregenfinished = true;
			config.autorestartcountdown = false;
			plugin.autorestartThread.startrestart();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private void deleteDirectory(File file)
	{
		if(!file.exists())  {return;}
	    if(file.isDirectory())
	    {
	    	for(File f : file.listFiles())
	    	{
	    		deleteDirectory(f);
	    	}
	    	file.delete();
	    }
	    else
	    {
	    	file.delete();
	    }
	}
	
}
