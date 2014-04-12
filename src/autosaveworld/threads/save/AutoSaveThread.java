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
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitScheduler;

import autosaveworld.config.AutoSaveWorldConfigMSG;
import autosaveworld.config.AutoSaveWorldConfig;
import autosaveworld.core.AutoSaveWorld;

public class AutoSaveThread extends Thread {

	private AutoSaveWorld plugin = null;
	private AutoSaveWorldConfig config;
	private AutoSaveWorldConfigMSG configmsg;

	public AutoSaveThread(AutoSaveWorld plugin, AutoSaveWorldConfig config, AutoSaveWorldConfigMSG configmsg) {
		this.plugin = plugin;
		this.config = config;
		this.configmsg = configmsg;
	}

	public void stopThread() {
		this.run = false;
	}

	public void startsave() {
		command = true;
	}

	private volatile boolean run = true;
	private boolean command = false;

	@Override
	public void run() {

		plugin.debug("AutoSaveThread Started");
		Thread.currentThread().setName("AutoSaveWorld AutoSaveThread");

		while (run) {
			// Prevent AutoSave from never sleeping
			// If interval is 0, sleep for 5 seconds and skip saving
			if (config.saveInterval == 0) {
				try {Thread.sleep(5000);} catch (InterruptedException e) {}
				continue;
			}

			// sleep
			for (int i = 0; i < config.saveInterval; i++) {
				if (!run || command) {break;}
				try {Thread.sleep(1000);} catch (InterruptedException e) {}
			}

			// save
			if (run && (config.saveEnabled || command)) {
				command = false;
				if (plugin.checkCanDoOperation()) {
					plugin.setOperationInProgress(true);
					try {
						performSave();
					} catch (Exception e) {
						e.printStackTrace();
					}
					plugin.setOperationInProgress(false);
				}
			}
		}

		plugin.debug("Graceful quit of AutoSaveThread");

	}

	public void performSaveNow() {
		plugin.broadcast(configmsg.messageSaveBroadcastPre, config.saveBroadcast);

		plugin.debug("Saving players");
		plugin.getServer().savePlayers();
		plugin.debug("Saved Players");
		plugin.debug("Saving worlds");
		for (World w : plugin.getServer().getWorlds()) {
			saveWorld(w);
		}
		plugin.debug("Saved Worlds");

		plugin.broadcast(configmsg.messageSaveBroadcastPost, config.saveBroadcast);
	}

	public void performSave() {
		plugin.broadcast(configmsg.messageSaveBroadcastPre, config.saveBroadcast);
		// Save the players
		plugin.debug("Saving players");
		BukkitScheduler scheduler = plugin.getServer().getScheduler();
		int taskid;
		if (run) {
			taskid = scheduler.scheduleSyncDelayedTask(plugin,
				new Runnable() {
					@Override
					public void run() {
						plugin.getServer().savePlayers();
					}
				}
			);
			while (scheduler.isCurrentlyRunning(taskid) || scheduler.isQueued(taskid)) {
				try {Thread.sleep(100);} catch (InterruptedException e) {}
			}
		}
		plugin.debug("Saved Players");
		// Save the worlds
		plugin.debug("Saving worlds");
		for (final World world : plugin.getServer().getWorlds()) {
			if (run) {
				taskid = scheduler.scheduleSyncDelayedTask(plugin,
					new Runnable() {
						@Override
						public void run() {
							plugin.debug(String.format("Saving world: %s", world.getName()));
							saveWorld(world);
						}
					}
				);
				while (scheduler.isCurrentlyRunning(taskid) || scheduler.isQueued(taskid)) {
					try {Thread.sleep(100);} catch (InterruptedException e) {}
				}
			}
		}
		plugin.debug("Saved Worlds");
		plugin.broadcast(configmsg.messageSaveBroadcastPost, config.saveBroadcast);
	}

	private void saveWorld(World world) {
		// structures are saved only for main world so we use this workaround
		// only for main world
		if (config.donotsavestructures && Bukkit.getWorlds().get(0).getName().equalsIgnoreCase(world.getName())) {
			saveWorldDoNoSaveStructureInfo(world);
		} else {
			saveWorldNormal(world);
		}
	}

	private void saveWorldNormal(World world) {
		world.save();
	}

	private void saveWorldDoNoSaveStructureInfo(World world) {
		// save saveenabled state
		boolean saveenabled = world.isAutoSave();
		// set saveenabled state
		world.setAutoSave(true);
		// now lets save everything besides structures
		try {
			// get worldserver and dataManager
			Field worldField = world.getClass().getDeclaredField("world");
			worldField.setAccessible(true);
			Object worldserver = worldField.get(world);
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
			Field serverField = worldserver.getClass().getDeclaredField("server");
			serverField.setAccessible(true);
			Object server = serverField.get(worldserver);
			Method getPlayerListMethod = server.getClass().getDeclaredMethod("getPlayerList");
			getPlayerListMethod.setAccessible(true);
			Object playerList = getPlayerListMethod.invoke(server);
			Method qMethod = playerList.getClass().getSuperclass().getDeclaredMethod("q");
			qMethod.setAccessible(true);
			Object NBTTagCompound = qMethod.invoke(playerList);
			Method saveWorldDataMethod = dataManager.getClass().getSuperclass().getDeclaredMethod("saveWorldData", worldDataField.getType(), qMethod.getReturnType());
			saveWorldDataMethod.invoke(dataManager, worldData, NBTTagCompound);
			// invoke saveChunks
			Field chunkProviderField = worldserver.getClass().getSuperclass().getDeclaredField("chunkProvider");
			chunkProviderField.setAccessible(true);
			Object chunkProvider = chunkProviderField.get(worldserver);
			for (Method method : chunkProvider.getClass().getDeclaredMethods()) {
				if (method.getName().equals("saveChunks") && method.getParameterTypes().length == 2) {
					method.setAccessible(true);
					method.invoke(chunkProvider, true, null);
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			// failed to save using reflections, save world normal
			plugin.debug("failed to workaround stucture saving, saving world using normal methods");
			saveWorldNormal(world);
		}
		// reset saveenabled state
		world.setAutoSave(saveenabled);
	}

}
