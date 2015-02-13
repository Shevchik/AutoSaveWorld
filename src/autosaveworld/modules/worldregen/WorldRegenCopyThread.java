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

package autosaveworld.modules.worldregen;

import java.io.File;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import autosaveworld.config.AutoSaveWorldConfig;
import autosaveworld.config.AutoSaveWorldConfigMSG;
import autosaveworld.core.AutoSaveWorld;
import autosaveworld.core.GlobalConstants;
import autosaveworld.core.logging.MessageLogger;
import autosaveworld.modules.worldregen.plugins.FactionsDataProvider;
import autosaveworld.modules.worldregen.plugins.GriefPreventionDataProvider;
import autosaveworld.modules.worldregen.plugins.PStonesDataProvider;
import autosaveworld.modules.worldregen.plugins.TownyDataProvider;
import autosaveworld.modules.worldregen.plugins.WorldGuardDataProvider;
import autosaveworld.modules.worldregen.tasks.CopyTask;
import autosaveworld.threads.restart.RestartWaiter;
import autosaveworld.utils.ListenerUtils;
import autosaveworld.utils.SchedulerUtils;

public class WorldRegenCopyThread extends Thread {

	private AutoSaveWorld plugin;
	private AutoSaveWorldConfig config;
	private AutoSaveWorldConfigMSG configmsg;

	public WorldRegenCopyThread(AutoSaveWorld plugin, AutoSaveWorldConfig config, AutoSaveWorldConfigMSG configmsg) {
		this.plugin = plugin;
		this.config = config;
		this.configmsg = configmsg;
	}

	private String worldtoregen = "";

	public void setWorld(String worldname) {
		worldtoregen = worldname;
	}

	@Override
	public void run() {
		MessageLogger.debug("WorldRegenThread Started");
		Thread.currentThread().setName("AutoSaveWorld WorldRegenCopyThread");

		try {
			doWorldRegen();
		} catch (Throwable t) {
			MessageLogger.warn("Error occured while copying, worldregen stopped");
			t.printStackTrace();
		}
	}

	private void doWorldRegen() throws Exception {

		// kick all player and deny them from join
		ListenerUtils.registerListener(new AntiJoinListener(configmsg));
		SchedulerUtils.callSyncTaskAndWait(new Runnable() {
			@Override
			public void run() {
				for (Player p : Bukkit.getOnlinePlayers()) {
					MessageLogger.kickPlayer(p, configmsg.messageWorldRegenKick);
				}
			}
		});

		final World wtoregen = Bukkit.getWorld(worldtoregen);

		ArrayList<CopyTask> tasks = new ArrayList<CopyTask>();
		MessageLogger.debug("Saving buildings");
		if ((Bukkit.getPluginManager().getPlugin("WorldGuard") != null) && config.worldRegenSaveWG) {
			MessageLogger.debug("WG found, adding to copy list");
			tasks.add(new CopyTask(wtoregen, new WorldGuardDataProvider(wtoregen)));
		}
		if ((Bukkit.getPluginManager().getPlugin("Factions") != null) && config.worldRegenSaveFactions) {
			MessageLogger.debug("Factions found, adding to copy list");
			tasks.add(new CopyTask(wtoregen, new FactionsDataProvider(wtoregen)));
		}
		if ((Bukkit.getPluginManager().getPlugin("GriefPrevention") != null) && config.worldRegenSaveGP) {
			MessageLogger.debug("GriefPrevention found, adding to copy list");
			tasks.add(new CopyTask(wtoregen, new GriefPreventionDataProvider(wtoregen)));
		}
		if ((Bukkit.getPluginManager().getPlugin("Towny") != null) && config.worldregenSaveTowny) {
			MessageLogger.debug("Towny found, adding to copy list");
			tasks.add(new CopyTask(wtoregen, new TownyDataProvider(wtoregen)));
		}
		if ((Bukkit.getPluginManager().getPlugin("PreciousStones") != null) && config.worldregenSavePStones) {
			MessageLogger.debug("PreciousStones found, adding to copy list");
			tasks.add(new CopyTask(wtoregen, new PStonesDataProvider(wtoregen)));
		}
		for (CopyTask task : tasks) {
			task.doCopy();
		}
		MessageLogger.debug("Saving finished");

		if (config.worldRegenRemoveSeedData) {
			MessageLogger.debug("Removing seed data");
			new File(wtoregen.getWorldFolder(), "level.dat").delete();
			new File(wtoregen.getWorldFolder(), "level.dat_old").delete();
			new File(wtoregen.getWorldFolder(), "uid.dat").delete();
			MessageLogger.debug("Removing finished");
		}

		// Save worldname file
		FileConfiguration cfg = new YamlConfiguration();
		cfg.set("wname", worldtoregen);
		cfg.save(new File(GlobalConstants.getWorldnameFile()));

		MessageLogger.debug("Deleting map and restarting server");
		// Add hook that will delete world folder, signal that restart should wait, and schedule restart restart
		WorldRegenJVMshutdownhook wrsh = new WorldRegenJVMshutdownhook(wtoregen.getWorldFolder().getAbsolutePath());
		Runtime.getRuntime().addShutdownHook(wrsh);
		RestartWaiter.incrementWait();
		plugin.autorestartThread.startrestart(true);

	}

}
