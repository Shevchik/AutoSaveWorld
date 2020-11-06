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

package autosaveworld.features.worldregen;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import autosaveworld.config.AutoSaveWorldConfig;
import autosaveworld.core.AutoSaveWorld;
import autosaveworld.core.logging.MessageLogger;
import autosaveworld.features.restart.RestartWaiter;
import autosaveworld.features.worldregen.plugins.DataProvider;
import autosaveworld.features.worldregen.plugins.FactionsDataProvider;
import autosaveworld.features.worldregen.plugins.GriefPreventionDataProvider;
import autosaveworld.features.worldregen.plugins.PStonesDataProvider;
import autosaveworld.features.worldregen.plugins.TownyDataProvider;
import autosaveworld.features.worldregen.plugins.WorldGuardDataProvider;
import autosaveworld.features.worldregen.storage.AnvilRegion;
import autosaveworld.features.worldregen.storage.Coord;
import autosaveworld.features.worldregen.storage.WorldMap;
import autosaveworld.utils.BukkitUtils;
import autosaveworld.utils.FileUtils;
import autosaveworld.utils.SchedulerUtils;

public class WorldRegenThread extends Thread {

	private final String worldtoregen;
	private final String worldRegionFolder;
	public WorldRegenThread(String worldtoregen, String worldRegionFolder) {
		super("AutoSaveWorld WorldRegenCopyThread");
		this.worldtoregen = worldtoregen;
		this.worldRegionFolder = worldRegionFolder;
	}

	@Override
	public void run() {
		MessageLogger.debug("WorldRegenThread Started");
		doWorldRegen();
	}

	private void doWorldRegen() {

		BukkitUtils.registerListener(new AntiJoinListener());
		SchedulerUtils.callSyncTaskAndWait(new Runnable() {
			@Override
			public void run() {
				for (Player p : BukkitUtils.getOnlinePlayers()) {
					MessageLogger.kickPlayer(p, AutoSaveWorld.getInstance().getMessageConfig().messageWorldRegenKick);
				}
			}
		});

		final World wtoregen = Bukkit.getWorld(worldtoregen);

		final ArrayList<DataProvider> providers = new ArrayList<DataProvider>();

		AutoSaveWorldConfig config = AutoSaveWorld.getInstance().getMainConfig();
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
			MessageLogger.exception("Failed to initialize preserve chunk list", t);
			return;
		}

		final WorldMap preservechunks = new WorldMap(config.worldRegenPreserveRadius);
		for (DataProvider provider : providers) {
			for (Coord chunkCoord : provider.getChunks()) {
				preservechunks.addChunk(chunkCoord);
			}
		}

		ArrayList<WorldRegenTask> tasks = new ArrayList<WorldRegenTask>();

		WorldRegenTask clearchunks = new WorldRegenTask() {
			@Override
			public void run() throws Throwable {
				File regionfolder = new File(worldRegionFolder);
				for (File regionfile : FileUtils.safeListFiles(regionfolder)) {
					MessageLogger.printOut("Processing regionfile "+regionfile.getName());
					try {
						AnvilRegion region = new AnvilRegion(regionfolder, regionfile.getName());
						if (preservechunks.hasChunks(region.getX(), region.getZ())) {
							region.loadFromDisk();
							Set<Coord> localChunks = preservechunks.getChunks(region.getX(), region.getZ());
							for (Coord columnchunk : region.getChunks()) {
								if (!localChunks.contains(columnchunk)) {
									region.removeChunk(columnchunk);
								}
							}
							region.saveToDisk();
						} else {
							region.delete();
						}
					} catch (Throwable e) {
						MessageLogger.printOut("Failed to process regionfile "+regionfile.getName());
						MessageLogger.printOutException(e);
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

		AutoSaveWorld.getInstance().getRestartThread().triggerRestart(true);
	}

}
