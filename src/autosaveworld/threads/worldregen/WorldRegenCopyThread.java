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
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import autosaveworld.config.AutoSaveConfig;
import autosaveworld.config.AutoSaveConfigMSG;
import autosaveworld.core.AutoSaveWorld;
import autosaveworld.threads.worldregen.factions.FactionsCopy;
import autosaveworld.threads.worldregen.wg.WorldGuardCopy;

public class WorldRegenCopyThread extends Thread {

	protected final Logger log = Bukkit.getLogger();
	
	
	private AutoSaveWorld plugin = null;
	private AutoSaveConfig config;
	private AutoSaveConfigMSG configmsg;
	private boolean run = true;
	
	private boolean doregen = false;
	
	private String worldtoregen = "";
	private int taskid;
	
	public WorldRegenCopyThread(AutoSaveWorld plugin, AutoSaveConfig config, AutoSaveConfigMSG configmsg)
	{
		this.plugin = plugin;
		this.config = config;
		this.configmsg = configmsg;
	}
	
	// Allows for the thread to naturally exit if value is false
	public void stopThread() {
		this.run = false;
	}
	
	public void startworldregen(String worldname) {
		doregen = true;
		this.worldtoregen = worldname;
	}
	
	public boolean isRegenerationInProcess()
	{
		return doregen;
	}
	
	
	public void run()
	{
		log.info("[AutoSaveWorld] WorldRegenThread Started");
		
		Thread.currentThread().setName("AutoSaveWorld WorldRegenThread");
		
		while (run)
		{
			if (doregen)
			{
				try {
				doWorldRegen();
				} catch (Exception e) {
					e.printStackTrace();
				}
				doregen = false;
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		if (config.varDebug) {log.info("[AutoSaveWorld] Graceful quit of WorldRegenThread");}
	}
	
	
	private void doWorldRegen() throws Exception
	{
		final World wtoregen = Bukkit.getWorld(worldtoregen);
		
		FileConfiguration cfg = new YamlConfiguration();
		cfg.set("wname", worldtoregen);
		cfg.save(new File("plugins/AutoSaveWorld/WorldRegenTemp/wname.yml"));
		
		//kick all player and deny them from join
		AntiJoinListener jl = new AntiJoinListener(plugin,configmsg);
		Bukkit.getPluginManager().registerEvents(jl, plugin);
		taskid = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
		{
			public void run()
			{
				for (Player p : Bukkit.getOnlinePlayers())
				{
					p.kickPlayer("[AutoSaveWorld] server is regenerating map, please come back later");
				}
			}
		});
		while (Bukkit.getScheduler().isCurrentlyRunning(taskid) || Bukkit.getScheduler().isQueued(taskid))
		{
				Thread.sleep(1000);
		}
		
		plugin.debug("Saving buildings");
		
		//save WorldGuard buildings
		if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null && config.worldregensavewg)
		{
			new WorldGuardCopy(plugin, worldtoregen).copyAllToSchematics();
		}
		
		//save Factions homes
		if (Bukkit.getPluginManager().getPlugin("Factions") != null && config.worldregensavefactions)
		{
			new FactionsCopy(plugin, worldtoregen).copyAllToSchematics();
		}
		
		plugin.debug("Saving finished");
		
		//Shutdown server and delegate world removal to JVMShutdownHook
		plugin.debug("Deleting map and restarting server");
		WorldRegenJVMshutdownhook wrsh = new WorldRegenJVMshutdownhook(wtoregen.getWorldFolder().getCanonicalPath());
		Runtime.getRuntime().addShutdownHook(wrsh);
		plugin.autorestartThread.startrestart();
	}
	
	
}
