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
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import autosaveworld.config.AutoSaveConfig;
import autosaveworld.config.AutoSaveConfigMSG;
import autosaveworld.core.AutoSaveWorld;
import autosaveworld.threads.restart.RestartJVMshutdownhook;
import autosaveworld.threads.worldregen.factions.FactionsCopy;
import autosaveworld.threads.worldregen.griefprevention.GPCopy;
import autosaveworld.threads.worldregen.towny.TownyCopy;
import autosaveworld.threads.worldregen.wg.WorldGuardCopy;

public class WorldRegenCopyThread extends Thread {

	private AutoSaveWorld plugin = null;
	private AutoSaveConfig config;
	private AutoSaveConfigMSG configmsg;
	private RestartJVMshutdownhook jvmsh;
	public WorldRegenCopyThread(AutoSaveWorld plugin, AutoSaveConfig config, AutoSaveConfigMSG configmsg, RestartJVMshutdownhook jvmsh)
	{
		this.plugin = plugin;
		this.config = config;
		this.configmsg = configmsg;
		this.jvmsh = jvmsh;
	}

	// Allows for the thread to naturally exit if value is false
	public void stopThread() {
		this.run = false;
	}

	public void startworldregen(String worldname) {
		this.worldtoregen = worldname;
		plugin.worldregenInProcess = true;
		doregen = true;
	}


	private String worldtoregen = "";
	private int taskid;
	private volatile boolean run = true;
	private boolean doregen = false;
	@Override
	public void run()
	{
		plugin.debug("WorldRegenThread Started");
		Thread.currentThread().setName("AutoSaveWorld WorldRegenCopyThread");

		while (run)
		{
			if (doregen)
			{
				if (plugin.backupInProgress) {
					plugin.warn("AutoBackup is in process. WorldRegen cancelled.");
					return;
				}
				if (plugin.purgeInProgress) {
					plugin.warn("AutoPurge is in process. WorldRegen cancelled.");
					return;
				}
				try {
					doWorldRegen();
				} catch (Exception e) {
					e.printStackTrace();
				}
				doregen = false;
				run = false;
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		plugin.debug("Graceful quit of WorldRegenThread");
	}


	private void doWorldRegen() throws Exception
	{
		final World wtoregen = Bukkit.getWorld(worldtoregen);

		FileConfiguration cfg = new YamlConfiguration();
		cfg.set("wname", worldtoregen);
		cfg.save(new File(plugin.constants.getWorldnameFile()));

		//kick all player and deny them from join
		AntiJoinListener ajl = new AntiJoinListener(plugin,configmsg);
		Bukkit.getPluginManager().registerEvents(ajl, plugin);
		taskid = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
		{
			@Override
			public void run()
			{
				for (Player p : Bukkit.getOnlinePlayers())
				{
					plugin.kickPlayer(p,configmsg.messageWorldRegenKick);
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
			new WorldGuardCopy(plugin, this, worldtoregen).copyAllToSchematics();
		}

		//save Factions homes
		if (Bukkit.getPluginManager().getPlugin("Factions") != null && config.worldregensavefactions)
		{
			new FactionsCopy(plugin, this, worldtoregen).copyAllToSchematics();
		}

		//save GriefPrevention claims
		if (Bukkit.getPluginManager().getPlugin("GriefPrevention") != null && config.worldregensavegp)
		{
			new GPCopy(plugin, this, worldtoregen).copyAllToSchematics();
		}
		/*
		//save Towny towns
		if (Bukkit.getPluginManager().getPlugin("Towny") != null)
		{
			new TownyCopy(plugin, this, worldtoregen).copyAllToSchematics();
		}
		 */

		plugin.debug("Saving finished");

		//Shutdown server and delegate world removal to JVMShutdownHook
		plugin.debug("Deleting map and restarting server");
		jvmsh.setPath(config.autorestartscriptpath);
		WorldRegenJVMshutdownhook wrsh = new WorldRegenJVMshutdownhook(jvmsh, wtoregen.getWorldFolder().getCanonicalPath(), plugin.constants.getShouldpasteFile());
		Runtime.getRuntime().addShutdownHook(wrsh);
		plugin.getServer().shutdown();
	}

	private SchematicOperations schemops = null;
	public SchematicOperations getSchematicOperations()
	{
		if (schemops == null)
		{
			schemops = new SchematicOperations(plugin);
		}
		return schemops;
	}

}
