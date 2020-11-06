/**
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 */

package autosaveworld.core;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import autosaveworld.commands.CommandsHandler;
import autosaveworld.commands.NoTabCompleteCommandsHandler;
import autosaveworld.commands.subcommands.StopCommand;
import autosaveworld.config.AutoSaveWorldConfig;
import autosaveworld.config.AutoSaveWorldConfigMSG;
import autosaveworld.config.loader.ConfigLoader;
import autosaveworld.core.logging.MessageLogger;
import autosaveworld.features.backup.AutoBackupThread;
import autosaveworld.features.consolecommand.AutoConsoleCommandThread;
import autosaveworld.features.networkwatcher.NetworkWatcher;
import autosaveworld.features.purge.AutoPurgeThread;
import autosaveworld.features.restart.AutoRestartThread;
import autosaveworld.features.restart.CrashRestartThread;
import autosaveworld.features.restart.RestartShutdownHook;
import autosaveworld.features.restart.RestartWaiter;
import autosaveworld.features.save.AutoSaveThread;
import autosaveworld.utils.FileUtils;
import autosaveworld.utils.ReflectionUtils;
import autosaveworld.utils.SchedulerUtils;
import autosaveworld.utils.StringUtils;
import autosaveworld.utils.Threads.SIntervalTaskThread;

public class AutoSaveWorld extends JavaPlugin {

	private static AutoSaveWorld instance;

	public static AutoSaveWorld getInstance() {
		if (instance == null) {
			throw new IllegalStateException("Instance access before init");
		}
		return instance;
	}

	public AutoSaveWorld() {
		if (!Bukkit.isPrimaryThread()) {
			throw new IllegalStateException("Init not fom main thread");
		}
		if (instance != null) {
			MessageLogger.warn("Instance wasn't null when enabling, this is not a good sign");
		}
		instance = this;
		//important to create instance here
		config = new AutoSaveWorldConfig();
		configmsg = new AutoSaveWorldConfigMSG();
		saveThread = new AutoSaveThread();
		backupThread = new AutoBackupThread();
		purgeThread = new AutoPurgeThread();
		autorestartThread = new AutoRestartThread();
		crashrestartThread = new CrashRestartThread(Thread.currentThread());
		consolecommandThread = new AutoConsoleCommandThread();
		watcher = new NetworkWatcher();
	}

	private final AutoSaveWorldConfig config;
	private final AutoSaveWorldConfigMSG configmsg;

	private final AutoSaveThread saveThread;
	private final AutoBackupThread backupThread;
	private final AutoPurgeThread purgeThread;
	private final AutoRestartThread autorestartThread;
	private final CrashRestartThread crashrestartThread;
	private final AutoConsoleCommandThread consolecommandThread;
	private final NetworkWatcher watcher;

	public AutoSaveWorldConfig getMainConfig() {
		return config;
	}

	public AutoSaveWorldConfigMSG getMessageConfig() {
		return configmsg;
	}

	public AutoSaveThread getSaveThread() {
		return saveThread;
	}

	public AutoBackupThread getBackupThread() {
		return backupThread;
	}

	public AutoPurgeThread getPurgeThread() {
		return purgeThread;
	}

	public AutoRestartThread getRestartThread() {
		return autorestartThread;
	}

	@Override
	public void onEnable() {
		ConfigLoader.loadAndSave(config);
		ConfigLoader.loadAndSave(configmsg);
		preloadClasses();
		try {
			CommandsHandler commandshandler = new CommandsHandler();
			commandshandler.initSubCommandHandlers();
			for (String commandName : getDescription().getCommands().keySet()) {
				getCommand(commandName).setExecutor(commandshandler);
			}
		} catch (Throwable t) {
			NoTabCompleteCommandsHandler commandshandler = new NoTabCompleteCommandsHandler();
			commandshandler.initSubCommandHandlers();
			for (String commandName : getDescription().getCommands().keySet()) {
				getCommand(commandName).setExecutor(commandshandler);
			}
		}
		saveThread.start();
		backupThread.start();
		purgeThread.start();
		autorestartThread.start();
		crashrestartThread.start();
		consolecommandThread.start();
		watcher.register();
	}

	private static void preloadClasses() {
		//preload core classes, so replacing jar file won't break plugin completely (Some core functions should work)
		SchedulerUtils.init();
		ReflectionUtils.init();
		FileUtils.init();
		StringUtils.init();
		RestartWaiter.init();
	}

	@Override
	public void onDisable() {
		if (config.restartOnCrashOnNonAswStop && !StopCommand.isStoppedByAsw()) {
			MessageLogger.debug("Restarting due to server stopped not by asw command");
			Runtime.getRuntime().addShutdownHook(new RestartShutdownHook(new File(config.restartOnCrashScriptPath)));
		}
		if (config.saveOnASWDisable) {
			MessageLogger.debug("Saving");
			saveThread.performSaveNow();
		}
		ConfigLoader.save(config);
		ConfigLoader.save(configmsg);
		stopThread(saveThread);
		stopThread(backupThread);
		stopThread(purgeThread);
		stopThread(autorestartThread);
		stopThread(crashrestartThread);
		stopThread(consolecommandThread);
		watcher.unregister();
	}

	private static void stopThread(SIntervalTaskThread tt) {
		tt.stopThread();
		try {
			tt.join(2000);
		} catch (InterruptedException e) {
		}
	}

}
