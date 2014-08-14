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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.World;

import autosaveworld.config.AutoSaveWorldConfig;
import autosaveworld.config.AutoSaveWorldConfigMSG;
import autosaveworld.core.logging.MessageLogger;
import autosaveworld.threads.backup.AutoBackupThread;
import autosaveworld.utils.SchedulerUtils;

public class AutoSaveThread extends Thread {

	private AutoSaveWorldConfig config;
	private AutoSaveWorldConfigMSG configmsg;

	public AutoSaveThread(AutoSaveWorldConfig config, AutoSaveWorldConfigMSG configmsg) {
		this.config = config;
		this.configmsg = configmsg;
	}

	public void stopThread() {
		run = false;
	}

	public void startsave() {
		command = true;
	}

	private volatile boolean run = true;
	private boolean command = false;

	@Override
	public void run() {

		MessageLogger.debug("AutoSaveThread Started");
		Thread.currentThread().setName("AutoSaveWorld AutoSaveThread");

		while (run) {
			// sleep
			for (int i = 0; i < config.saveInterval; i++) {
				if (!run || command) {
					break;
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}

			// save
			if (run && (config.saveEnabled || command)) {
				command = false;
				try {
					performSave();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		MessageLogger.debug("Graceful quit of AutoSaveThread");

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

		if (!config.backupDisableWorldSaving && AutoBackupThread.backupRunning) {
			MessageLogger.debug("Backup is running with world saving enabled, skipping autosave");
			return;
		}

		MessageLogger.broadcast(configmsg.messageSaveBroadcastPre, config.saveBroadcast);

		// Save the players
		MessageLogger.debug("Saving players");
		if (run) {
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
			if (run) {
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
				worldserver.getClass().getMethod("saveLevel").invoke(worldserver);
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
		// structures are saved only for main world so we use this workaround only for main world
		if (config.saveDisableStructureSaving && Bukkit.getWorlds().get(0).getName().equalsIgnoreCase(world.getName())) {
			saveWorldDoNoSaveStructureInfo(world);
		} else {
			saveWorldNormal(world);
		}
	}

	private void saveWorldNormal(World world) {
		world.save();
	}

	private void saveWorldDoNoSaveStructureInfo(World world) {
		try {
			// get worldserver and dataManager
			Object worldserver = getNMSWorld(world);
			Field dataManagerField = worldserver.getClass().getSuperclass().getDeclaredField("dataManager");
			dataManagerField.setAccessible(true);
			Object dataManager = dataManagerField.get(worldserver);
			// invoke check session
			Method checkSessionMethod = dataManager.getClass().getSuperclass().getDeclaredMethod("checkSession");
			checkSessionMethod.setAccessible(true);
			checkSessionMethod.invoke(dataManager);
			// invoke saveWorldData
			Field worldDataField = worldserver.getClass().getSuperclass().getDeclaredField("worldData");
			worldDataField.setAccessible(true);
			Object worldData = worldDataField.get(worldserver);
			boolean methodfound = false;
			for (Method method : dataManager.getClass().getMethods()) {
				if (method.getName().equals("saveWorldData") && (method.getParameterTypes().length == 2)) {
					method.invoke(dataManager, worldData, null);
					methodfound = true;
					break;
				}
			}
			if (!methodfound) {
				throw new RuntimeException("Cannot find method saveWorldData");
			}
			// invoke saveChunks
			methodfound = false;
			Field chunkProviderField = worldserver.getClass().getSuperclass().getDeclaredField("chunkProvider");
			chunkProviderField.setAccessible(true);
			Object chunkProvider = chunkProviderField.get(worldserver);
			for (Method method : chunkProvider.getClass().getMethods()) {
				if (method.getName().equals("saveChunks") && (method.getParameterTypes().length == 2)) {
					method.invoke(chunkProvider, true, null);
					methodfound = true;
					break;
				}
			}
			if (!methodfound) {
				throw new RuntimeException("Cannot find method saveChunks");
			}
		} catch (Exception e) {
			MessageLogger.warn("failed to workaround stucture saving, saving world using normal methods");
			e.printStackTrace();
			// failed to save using reflections, save world normal
			saveWorldNormal(world);
		}
	}

	private Object getNMSWorld(World world) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Method getHandleMethod = world.getClass().getDeclaredMethod("getHandle");
		getHandleMethod.setAccessible(true);
		Object nmsWorld = getHandleMethod.invoke(world);
		return nmsWorld;
	}

}
