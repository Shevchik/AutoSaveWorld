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

package autosaveworld.threads.save;

import java.lang.reflect.InvocationTargetException;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;

import autosaveworld.config.AutoSaveWorldConfig;
import autosaveworld.config.AutoSaveWorldConfigMSG;
import autosaveworld.core.logging.MessageLogger;
import autosaveworld.threads.IntervalTaskThread;
import autosaveworld.threads.backup.AutoBackupThread;
import autosaveworld.utils.ReflectionUtils;
import autosaveworld.utils.SchedulerUtils;

public class AutoSaveThread extends IntervalTaskThread {

	private AutoSaveWorldConfig config;
	private AutoSaveWorldConfigMSG configmsg;

	public AutoSaveThread(AutoSaveWorldConfig config, AutoSaveWorldConfigMSG configmsg) {
		super("AutoSaveThread");
		this.config = config;
		this.configmsg = configmsg;
	}

	@Override
	public void onStart() {
		//disable bukkit built-in autosave
		try {
			Server server = Bukkit.getServer();
			Object minecraftserver = ReflectionUtils.getField(server.getClass(), "console").get(server);
			ReflectionUtils.getField(minecraftserver.getClass(), "autosavePeriod").set(minecraftserver, 0);
		} catch (Throwable t) {
		}
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public int getInterval() {
		return config.saveInterval;
	}

	@Override
	public void doTask() {
		performSave();
	}

	public void performSaveNow() {
		MessageLogger.broadcast(configmsg.messageSaveBroadcastPre, config.saveBroadcast);

		MessageLogger.debug("Saving players");
		Bukkit.savePlayers();
		MessageLogger.debug("Saved Players");
		MessageLogger.debug("Saving worlds");
		for (World w : Bukkit.getWorlds()) {
			saveWorld(w);
		}
		MessageLogger.debug("Saved Worlds");

		MessageLogger.broadcast(configmsg.messageSaveBroadcastPost, config.saveBroadcast);
	}

	public void performSave() {

		if (AutoBackupThread.backupRunning) {
			MessageLogger.debug("Backup is running, skipping autosave");
			return;
		}

		MessageLogger.broadcast(configmsg.messageSaveBroadcastPre, config.saveBroadcast);

		// Save the players
		MessageLogger.debug("Saving players");
		if (isRunning()) {
			SchedulerUtils.callSyncTaskAndWait(new Runnable() {
				@Override
				public void run() {
					Bukkit.savePlayers();
				}
			});
		}
		MessageLogger.debug("Saved Players");

		// Save the worlds
		MessageLogger.debug("Saving worlds");
		for (final World world : Bukkit.getWorlds()) {
			if (isRunning()) {
				SchedulerUtils.callSyncTaskAndWait(new Runnable() {
					@Override
					public void run() {
						saveWorld(world);
					}
				});
			}
		}
		MessageLogger.debug("Saved Worlds");

		// Dump region cache
		if (config.saveDumpRegionCache) {
			MessageLogger.debug("Dumping cache");
			for (World world : Bukkit.getWorlds()) {
				dumpRegionCache(world);
			}
			MessageLogger.debug("Dumped cache");
		}

		MessageLogger.broadcast(configmsg.messageSaveBroadcastPost, config.saveBroadcast);
	}

	private void dumpRegionCache(World world) {
		if (world.isAutoSave()) {
			try {
				Object worldserver = getNMSWorld(world);
				// invoke saveLevel method which waits for all chunks to save and than dumps RegionFileCache
				ReflectionUtils.getMethod(worldserver.getClass(), NMSNames.getSaveLevelMethodName(), 0).invoke(worldserver);
			} catch (Exception e) {
				MessageLogger.warn("Could not dump RegionFileCache");
				e.printStackTrace();
			}
		}
	}

	private void saveWorld(World world) {
		if (!world.isAutoSave()) {
			return;
		}

		if (config.saveDisableStructureSaving && needSaveWorkAround()) {
			saveWorldDoNoSaveStructureInfo(world);
		} else {
			saveWorldNormal(world);
		}
	}

	private boolean needSaveWorkAround() {
		return true;
	}

	private void saveWorldNormal(World world) {
		world.save();
	}

	private void saveWorldDoNoSaveStructureInfo(World world) {
		try {
			// get worldserver, dataManager, chunkProvider, worldData
			Object worldserver = getNMSWorld(world);
			Object dataManager = ReflectionUtils.getField(worldserver.getClass(), NMSNames.getDataManagerFieldName()).get(worldserver);
			Object chunkProvider = ReflectionUtils.getField(worldserver.getClass(), NMSNames.getChunkProviderFieldName()).get(worldserver);
			Object worldData = ReflectionUtils.getField(worldserver.getClass(), NMSNames.getWorldDataFieldName()).get(worldserver);
			// invoke check session
			ReflectionUtils.getMethod(dataManager.getClass(), NMSNames.getCheckSessionMethodName(), 0).invoke(dataManager);
			// invoke saveWorldData
			ReflectionUtils.getMethod(dataManager.getClass(), NMSNames.getSaveWorldDataMethodName(), 2).invoke(dataManager, worldData, null);
			// invoke saveChunks
			ReflectionUtils.getMethod(chunkProvider.getClass(), NMSNames.getSaveChunksMethodName(), 2).invoke(chunkProvider, true, null);
		} catch (Exception e) {
			MessageLogger.warn("failed to workaround stucture saving, saving world using normal methods");
			e.printStackTrace();
			// failed to save using reflection, save world using normal methods
			saveWorldNormal(world);
		}
	}

	private Object getNMSWorld(World world) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		return ReflectionUtils.getMethod(world.getClass(), "getHandle", 0).invoke(world);
	}

}
