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

import org.bukkit.plugin.java.JavaPlugin;

import autosaveworld.commands.CommandsHandler;
import autosaveworld.commands.NoTabCompleteCommandsHandler;
import autosaveworld.commands.subcommands.StopCommand;
import autosaveworld.config.AutoSaveWorldConfig;
import autosaveworld.config.AutoSaveWorldConfigMSG;
import autosaveworld.config.loader.ConfigLoader;
import autosaveworld.core.logging.MessageLogger;
import autosaveworld.features.ThreadType;
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

public class AutoSaveWorld extends JavaPlugin {

	// save
	public AutoSaveThread saveThread;
	// backup
	public AutoBackupThread backupThread;
	// purge
	public AutoPurgeThread purgeThread;
	// restart
	public CrashRestartThread crashrestartThread;
	public AutoRestartThread autorestartThread;
	// autoconsolecommand
	public AutoConsoleCommandThread consolecommandThread;

	private static AutoSaveWorld instance;

	public static AutoSaveWorld getInstance() {
		if (instance == null) {
			throw new IllegalStateException("Instance access before init");
		}
		return instance;
	}

	private final AutoSaveWorldConfig config = new AutoSaveWorldConfig();
	private final AutoSaveWorldConfigMSG configmsg = new AutoSaveWorldConfigMSG();

	private final NetworkWatcher watcher = new NetworkWatcher();

	public AutoSaveWorldConfig getMainConfig() {
		return config;
	}

	public AutoSaveWorldConfigMSG getMessageConfig() {
		return configmsg;
	}

	public AutoSaveWorld() {
		if (instance != null) {
			MessageLogger.warn("Instance wasn't null when enabling, this is not a good sign");
		}
		instance = this;
	}

	@Override
	public void onEnable() {
		GlobalConstants.init(this);
		ConfigLoader.loadAndSave(config);
		ConfigLoader.loadAndSave(configmsg);
		SchedulerUtils.init();
		ReflectionUtils.init();
		FileUtils.init();
		StringUtils.init();
		RestartWaiter.init();
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
		watcher.register();
		startThread(ThreadType.SAVE);
		startThread(ThreadType.BACKUP);
		startThread(ThreadType.PURGE);
		startThread(ThreadType.CRASHRESTART);
		startThread(ThreadType.AUTORESTART);
		startThread(ThreadType.CONSOLECOMMAND);
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
		MessageLogger.debug("Saving config");
		ConfigLoader.save(config);
		ConfigLoader.save(configmsg);
		MessageLogger.debug("Stopping Threads");
		stopThread(ThreadType.SAVE);
		stopThread(ThreadType.BACKUP);
		stopThread(ThreadType.PURGE);
		stopThread(ThreadType.CRASHRESTART);
		stopThread(ThreadType.AUTORESTART);
		stopThread(ThreadType.CONSOLECOMMAND);
		watcher.unregister();
	}

	protected void startThread(ThreadType type) {
		switch (type) {
			case SAVE: {
				if ((saveThread == null) || !saveThread.isAlive()) {
					saveThread = new AutoSaveThread();
					saveThread.start();
				}
				return;
			}
			case BACKUP: {
				if ((backupThread == null) || !backupThread.isAlive()) {
					backupThread = new AutoBackupThread();
					backupThread.start();
				}
				return;
			}
			case PURGE: {
				if ((purgeThread == null) || !purgeThread.isAlive()) {
					purgeThread = new AutoPurgeThread();
					purgeThread.start();
				}
				return;
			}
			case CRASHRESTART: {
				if ((crashrestartThread == null) || !crashrestartThread.isAlive()) {
					crashrestartThread = new CrashRestartThread(Thread.currentThread());
					crashrestartThread.start();
				}
				return;
			}
			case AUTORESTART: {
				if ((autorestartThread == null) || !autorestartThread.isAlive()) {
					autorestartThread = new AutoRestartThread();
					autorestartThread.start();
				}
				return;
			}
			case CONSOLECOMMAND: {
				if ((consolecommandThread == null) || !consolecommandThread.isAlive()) {
					consolecommandThread = new AutoConsoleCommandThread();
					consolecommandThread.start();
				}
				return;
			}
		}
	}

	protected void stopThread(ThreadType type) {
		switch (type) {
			case SAVE: {
				if (saveThread != null) {
					saveThread.stopThread();
					try {
						saveThread.join(2000);
						saveThread = null;
					} catch (InterruptedException e) {
						MessageLogger.warn("Could not stop AutoSaveThread");
					}
				}
				return;
			}
			case BACKUP: {
				if (backupThread != null) {
					backupThread.stopThread();
					try {
						backupThread.join(2000);
						backupThread = null;
					} catch (InterruptedException e) {
						MessageLogger.warn("Could not stop AutoBackupThread");
					}
				}
				return;
			}
			case PURGE: {
				if (purgeThread != null) {
					purgeThread.stopThread();
					try {
						purgeThread.join(2000);
						purgeThread = null;
					} catch (InterruptedException e) {
						MessageLogger.warn("Could not stop AutoPurgeThread");
					}
				}
				return;
			}
			case CRASHRESTART: {
				if (crashrestartThread != null) {
					crashrestartThread.stopThread();
					try {
						crashrestartThread.join(2000);
						crashrestartThread = null;
					} catch (InterruptedException e) {
						MessageLogger.warn("Could not stop CrashRestartThread");
					}
				}
				return;
			}
			case AUTORESTART: {
				if (autorestartThread != null) {
					autorestartThread.stopThread();
					try {
						autorestartThread.join(2000);
						autorestartThread = null;
					} catch (InterruptedException e) {
						MessageLogger.warn("Could not stop AutoRestartThread");
					}
				}
				return;
			}
			case CONSOLECOMMAND: {
				if (consolecommandThread != null) {
					consolecommandThread.stopThread();
					try {
						consolecommandThread.join(2000);
						consolecommandThread = null;
					} catch (InterruptedException e) {
						MessageLogger.warn("Could not stop ConsoleCommandThread");
					}
				}
				return;
			}
		}
	}

}
