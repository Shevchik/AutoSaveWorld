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
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import autosaveworld.config.AutoSaveWorldConfig;
import autosaveworld.config.AutoSaveWorldConfigMSG;
import autosaveworld.core.AutoSaveWorld;
import autosaveworld.core.logging.MessageLogger;
import autosaveworld.modules.worldregen.plugins.DataProvider;
import autosaveworld.modules.worldregen.plugins.FactionsDataProvider;
import autosaveworld.modules.worldregen.plugins.GriefPreventionDataProvider;
import autosaveworld.modules.worldregen.plugins.PStonesDataProvider;
import autosaveworld.modules.worldregen.plugins.TownyDataProvider;
import autosaveworld.modules.worldregen.plugins.WorldGuardDataProvider;
import autosaveworld.modules.worldregen.storage.AnvilRegion;
import autosaveworld.modules.worldregen.storage.Coord;
import autosaveworld.threads.restart.RestartWaiter;
import autosaveworld.utils.FileUtils;
import autosaveworld.utils.ListenerUtils;
import autosaveworld.utils.SchedulerUtils;

public class WorldRegenThread extends Thread {

	private AutoSaveWorld plugin;
	private AutoSaveWorldConfig config;
	private AutoSaveWorldConfigMSG configmsg;
	private String worldtoregen;

	public WorldRegenThread(AutoSaveWorld plugin, AutoSaveWorldConfig config, AutoSaveWorldConfigMSG configmsg, String worldtoregen) {
		this.plugin = plugin;
		this.config = config;
		this.configmsg = configmsg;
		this.worldtoregen = worldtoregen;
	}

	@Override
	public void run() {
		MessageLogger.debug("WorldRegenThread Started");
		Thread.currentThread().setName("AutoSaveWorld WorldRegenCopyThread");

		doWorldRegen();
	}

	private void doWorldRegen() {

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

		final ArrayList<DataProvider> providers = new ArrayList<DataProvider>();

		try {
			if ((Bukkit.getPluginManager().getPlugin("WorldGuard") != null) && config.worldRegenSaveWG) {
				MessageLogger.debug("WG found, adding to copy list");
				providers.add(new WorldGuardDataProvider(wtoregen));
			}
			if ((Bukkit.getPluginManager().getPlugin("Factions") != null) && config.worldRegenSaveFactions) {
				MessageLogger.debug("Factions found, adding to copy list");
				providers.add(new FactionsDataProvider(wtoregen));
			}
			if ((Bukkit.getPluginManager().getPlugin("GriefPrevention") != null) && config.worldRegenSaveGP) {
				MessageLogger.debug("GriefPrevention found, adding to copy list");
				providers.add(new GriefPreventionDataProvider(wtoregen));
			}
			if ((Bukkit.getPluginManager().getPlugin("Towny") != null) && config.worldregenSaveTowny) {
				MessageLogger.debug("Towny found, adding to copy list");
				providers.add(new TownyDataProvider(wtoregen));
			}
			if ((Bukkit.getPluginManager().getPlugin("PreciousStones") != null) && config.worldregenSavePStones) {
				MessageLogger.debug("PreciousStones found, adding to copy list");
				providers.add(new PStonesDataProvider(wtoregen));
			}
		} catch (Throwable t) {
			MessageLogger.warn("Failed to initialize preserve chunk list");
			t.printStackTrace();
			return;
		}

		ArrayList<WorldRegenTask> tasks = new ArrayList<WorldRegenTask>();

		WorldRegenTask clearchunks = new WorldRegenTask() {
			@Override
			public void run() throws Throwable {
				HashSet<Coord> preservechunks = new HashSet<Coord>(1500);
				for (DataProvider provider : providers) {
					preservechunks.addAll(provider.getChunks());
				}
				File regionfolder = new File(wtoregen.getWorldFolder(), "region");
				for (File regionfile : FileUtils.safeListFiles(regionfolder)) {
					try {
						AnvilRegion column = new AnvilRegion(regionfolder, regionfile.getName());
						for (Coord columnchunk : column.getChunks()) {
							if (!preservechunks.contains(columnchunk)) {
								column.removeChunk(columnchunk);
							}
						}
						column.saveToDisk();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					}
				}
			}
		};
		tasks.add(clearchunks);

		if (config.worldRegenRemoveSeedData) {
			WorldRegenTask removeseed = new WorldRegenTask() {
				@Override
				public void run() throws Throwable {
					new File(wtoregen.getWorldFolder(), "level.dat").delete();
					new File(wtoregen.getWorldFolder(), "level.dat_old").delete();
					new File(wtoregen.getWorldFolder(), "uid.dat").delete();
				}
			};
			tasks.add(removeseed);
		}

		MessageLogger.debug("Stopping server and adding shutdown hook to perform needed actions");

		WorldRegenJVMshutdownhook wrsh = new WorldRegenJVMshutdownhook(tasks);
		Runtime.getRuntime().addShutdownHook(wrsh);
		RestartWaiter.incrementWait();

		plugin.autorestartThread.startrestart(true);
	}

}
