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

import org.bukkit.plugin.java.JavaPlugin;

import autosaveworld.commands.CommandsHandler;
import autosaveworld.commands.NoTabCompleteCommandsHandler;
import autosaveworld.commands.subcommands.StopCommand;
import autosaveworld.config.AutoSaveWorldConfig;
import autosaveworld.config.AutoSaveWorldConfigMSG;
import autosaveworld.config.LocaleChanger;
import autosaveworld.config.loader.ConfigLoader;
import autosaveworld.core.logging.MessageLogger;
import autosaveworld.modules.networkwatcher.NetworkWatcher;
import autosaveworld.modules.pluginmanager.PluginManager;
import autosaveworld.modules.processmanager.ProcessManager;
import autosaveworld.threads.ThreadType;
import autosaveworld.threads.backup.AutoBackupThread;
import autosaveworld.threads.consolecommand.AutoConsoleCommandThread;
import autosaveworld.threads.purge.AutoPurgeThread;
import autosaveworld.threads.restart.AutoRestartThread;
import autosaveworld.threads.restart.CrashRestartThread;
import autosaveworld.threads.restart.RestartShutdownHook;
import autosaveworld.threads.save.AutoSaveThread;
import autosaveworld.utils.CommandUtils;
import autosaveworld.utils.FileUtils;
import autosaveworld.utils.ListenerUtils;
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
	private RestartShutdownHook JVMsh;
	// autoconsolecommand
	public AutoConsoleCommandThread consolecommandThread;
	// plugin manager
	public PluginManager pluginmanager;
	// process manager
	public ProcessManager processmanager;
	// network watcher
	public NetworkWatcher watcher;
	// configs
	public AutoSaveWorldConfigMSG configmsg;
	public AutoSaveWorldConfig config;

	@Override
	public void onEnable() {
		// Init global constants
		GlobalConstants.init(this);
		// Load main config
		config = new AutoSaveWorldConfig();
		ConfigLoader.loadAndSave(config);
		// Init logger and message utils
		MessageLogger.init(getLogger(), config);
		// Init other utils
		SchedulerUtils.init(this);
		ListenerUtils.init(this);
		ReflectionUtils.init();
		CommandUtils.init();
		FileUtils.init();
		StringUtils.init();
		// Load messages
		configmsg = new AutoSaveWorldConfigMSG();
		ConfigLoader.loadAndSave(configmsg);
		// Register commands
		try {
			CommandsHandler commandshandler = new CommandsHandler(this, config, configmsg, new LocaleChanger(this, configmsg));
			commandshandler.initSubCommandHandlers();
			for (String commandName : getDescription().getCommands().keySet()) {
				getCommand(commandName).setExecutor(commandshandler);
			}
		} catch (Throwable t) {
			NoTabCompleteCommandsHandler commandshandler = new NoTabCompleteCommandsHandler(this, config, configmsg, new LocaleChanger(this, configmsg));
			commandshandler.initSubCommandHandlers();
			for (String commandName : getDescription().getCommands().keySet()) {
				getCommand(commandName).setExecutor(commandshandler);
			}
		}
		// Load plugin manager
		pluginmanager = new PluginManager();
		// Load process manager
		processmanager = new ProcessManager();
		// Load network watcher
		watcher = new NetworkWatcher(config);
		watcher.register();
		// Start Threads
		startThread(ThreadType.SAVE);
		startThread(ThreadType.BACKUP);
		startThread(ThreadType.PURGE);
		JVMsh = new RestartShutdownHook();
		startThread(ThreadType.CRASHRESTART);
		startThread(ThreadType.AUTORESTART);
		startThread(ThreadType.CONSOLECOMMAND);
	}

	@Override
	public void onDisable() {
		if (config.restartOnCrashOnNonAswStop && !StopCommand.isStoppedByAsw()) {
			MessageLogger.debug("Restarting due to server stopped not by asw command");
			JVMsh.setPath(config.restartOnCrashScriptPath);
			Runtime.getRuntime().addShutdownHook(JVMsh);
		}
		if (config.saveOnASWDisable) {
			// Perform a Save NOW!
			MessageLogger.debug("Saving");
			saveThread.performSaveNow();
		}
		// Save config
		MessageLogger.debug("Saving config");
		ConfigLoader.save(config);
		ConfigLoader.save(configmsg);
		// Stop threads
		MessageLogger.debug("Stopping Threads");
		stopThread(ThreadType.SAVE);
		stopThread(ThreadType.BACKUP);
		stopThread(ThreadType.PURGE);
		stopThread(ThreadType.CRASHRESTART);
		stopThread(ThreadType.AUTORESTART);
		JVMsh = null;
		stopThread(ThreadType.CONSOLECOMMAND);
		// stop network watcher
		watcher.unregister();
		watcher = null;
		// null some variables
		pluginmanager = null;
		processmanager = null;
		configmsg = null;
		config = null;
	}

	protected void startThread(ThreadType type) {
		switch (type) {
			case SAVE: {
				if ((saveThread == null) || !saveThread.isAlive()) {
					saveThread = new AutoSaveThread(config, configmsg);
					saveThread.start();
				}
				return;
			}
			case BACKUP: {
				if ((backupThread == null) || !backupThread.isAlive()) {
					backupThread = new AutoBackupThread(this, config, configmsg);
					backupThread.start();
				}
				return;
			}
			case PURGE: {
				if ((purgeThread == null) || !purgeThread.isAlive()) {
					purgeThread = new AutoPurgeThread(config, configmsg);
					purgeThread.start();
				}
				return;
			}
			case CRASHRESTART: {
				if ((crashrestartThread == null) || !crashrestartThread.isAlive()) {
					crashrestartThread = new CrashRestartThread(Thread.currentThread(), config, JVMsh);
					crashrestartThread.start();
				}
				return;
			}
			case AUTORESTART: {
				if ((autorestartThread == null) || !autorestartThread.isAlive()) {
					autorestartThread = new AutoRestartThread(config, configmsg, JVMsh);
					autorestartThread.start();
				}
				return;
			}
			case CONSOLECOMMAND: {
				if ((consolecommandThread == null) || !consolecommandThread.isAlive()) {
					consolecommandThread = new AutoConsoleCommandThread(config);
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
