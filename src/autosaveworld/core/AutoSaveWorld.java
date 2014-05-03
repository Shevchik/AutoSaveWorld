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
import autosaveworld.config.AutoSaveWorldConfig;
import autosaveworld.config.AutoSaveWorldConfigMSG;
import autosaveworld.config.LocaleChanger;
import autosaveworld.core.logging.MessageLogger;
import autosaveworld.listener.EventsListener;
import autosaveworld.modules.pluginmanager.PluginManager;
import autosaveworld.modules.processmanager.ProcessManager;
import autosaveworld.threads.ThreadType;
import autosaveworld.threads.backup.AutoBackupThread;
import autosaveworld.threads.consolecommand.AutoConsoleCommandThread;
import autosaveworld.threads.purge.AutoPurgeThread;
import autosaveworld.threads.restart.AutoRestartThread;
import autosaveworld.threads.restart.CrashRestartThread;
import autosaveworld.threads.restart.RestartJVMshutdownhook;
import autosaveworld.threads.save.AutoSaveThread;
import autosaveworld.threads.worldregen.WorldRegenCopyThread;
import autosaveworld.threads.worldregen.WorldRegenPasteThread;
import autosaveworld.utils.SchedulerUtils;

public class AutoSaveWorld extends JavaPlugin {

	//save
	public AutoSaveThread saveThread = null;
	//backup
	public AutoBackupThread backupThread = null;
	//purge
	public AutoPurgeThread purgeThread = null;
	//restart
	public CrashRestartThread crashrestartThread = null;
	public AutoRestartThread autorestartThread = null;
	private RestartJVMshutdownhook JVMsh = null;
	//autoconsolecommand
	public AutoConsoleCommandThread consolecommandThread = null;
	//worldregen
	public WorldRegenCopyThread worldregencopyThread = null;
	public WorldRegenPasteThread worldregenpasteThread = null;
	//plugin manager
	public PluginManager pmanager;
	//process manager
	public ProcessManager processmanager;
	//configs
	public AutoSaveWorldConfigMSG configmsg;
	public AutoSaveWorldConfig config;
	public LocaleChanger localeChanger;
	//event listener
	public EventsListener eh;
	//command executor
	public CommandsHandler ch;
	//operation lock
	private boolean operationInProgress = false;
	public void setOperationInProgress(boolean inProgress) {
		operationInProgress = inProgress;
	}
	public boolean checkCanDoOperation() {
		if (operationInProgress) {
			MessageLogger.warn("Other autosaveworld operation is in progress, current operation aborted");
			return false;
		}
		return true;
	}

	@Override
	public void onEnable() {
		new GlobalConstants(this);
		new SchedulerUtils(this);
		config = new AutoSaveWorldConfig();
		config.load();
		configmsg = new AutoSaveWorldConfigMSG();
		configmsg.loadmsg();
		new MessageLogger(getLogger(), config);
		localeChanger = new LocaleChanger(this, configmsg);
		eh = new EventsListener(this,config);
		ch = new CommandsHandler(this,config,configmsg,localeChanger);
		// Load plugin manager
		pmanager = new PluginManager(this);
		// Load process manager
		processmanager = new ProcessManager();
		// Register events and commands
		getCommand("autosaveworld").setExecutor(ch);
		getCommand("autosave").setExecutor(ch);
		getCommand("autobackup").setExecutor(ch);
		getCommand("autopurge").setExecutor(ch);
		getServer().getPluginManager().registerEvents(eh, this);
		// Start Threads
		startThread(ThreadType.SAVE);
		startThread(ThreadType.BACKUP);
		startThread(ThreadType.PURGE);
		JVMsh = new RestartJVMshutdownhook();
		startThread(ThreadType.CRASHRESTART);
		startThread(ThreadType.AUTORESTART);
		startThread(ThreadType.CONSOLECOMMAND);
		startThread(ThreadType.WORLDREGENCOPY);
		startThread(ThreadType.WORLDREGENPASTE);
	}


	@Override
	public void onDisable() {
		if (config.saveOnASWDisable) {
			// Perform a Save NOW!
			MessageLogger.debug("Saving");
			saveThread.performSaveNow();
		}
		// Save config
		MessageLogger.debug("Saving config");
		config.save();
		// Stop threads
		MessageLogger.debug("Stopping Threads");
		stopThread(ThreadType.SAVE);
		stopThread(ThreadType.BACKUP);
		stopThread(ThreadType.PURGE);
		stopThread(ThreadType.CRASHRESTART);
		stopThread(ThreadType.AUTORESTART);
		JVMsh = null;
		stopThread(ThreadType.CONSOLECOMMAND);
		stopThread(ThreadType.WORLDREGENCOPY);
		stopThread(ThreadType.WORLDREGENPASTE);
		//null plugin manager
		pmanager = null;
		//null process manager
		processmanager = null;
		//null values
		configmsg = null;
		config = null;
		localeChanger = null;
		eh = null;
		ch = null;
	}



	protected boolean startThread(ThreadType type) {
		switch (type) {
			case SAVE: {
				if (saveThread == null || !saveThread.isAlive()) {
					saveThread = new AutoSaveThread(this, config, configmsg);
					saveThread.start();
				}
				return true;
			}
			case BACKUP: {
				if (backupThread == null || !backupThread.isAlive()) {
					backupThread = new AutoBackupThread(this, config, configmsg);
					backupThread.start();
				}
				return true;
			}
			case PURGE: {
				if (purgeThread == null || !purgeThread.isAlive()) {
					purgeThread = new AutoPurgeThread(this, config, configmsg);
					purgeThread.start();
				}
				return true;
			}
			case CRASHRESTART: {
				if (crashrestartThread == null || !crashrestartThread.isAlive()) {
					crashrestartThread = new CrashRestartThread(config, JVMsh);
					crashrestartThread.start();
				}
				return true;
			}
			case AUTORESTART: {
				if (autorestartThread == null || !autorestartThread.isAlive()) {
					autorestartThread = new AutoRestartThread(config, configmsg, JVMsh);
					autorestartThread.start();
				}
				return true;
			}
			case CONSOLECOMMAND: {
				if (consolecommandThread == null || !consolecommandThread.isAlive()) {
					consolecommandThread = new AutoConsoleCommandThread(config);
					consolecommandThread.start();
				}
				return true;
			}
			case WORLDREGENCOPY: {
				if (worldregencopyThread == null || !worldregencopyThread.isAlive()) {
					worldregencopyThread = new WorldRegenCopyThread(this, config ,configmsg);
					worldregencopyThread.start();
				}
				return true;
			}
			case WORLDREGENPASTE: {
				if (worldregenpasteThread == null || !worldregenpasteThread.isAlive()) {
					worldregenpasteThread = new WorldRegenPasteThread(this, config, configmsg);
					worldregenpasteThread.checkIfShouldPaste();
					worldregenpasteThread.start();
				}
				return true;
			}
			default: {
				return false;
			}
		}
	}



	protected boolean stopThread(ThreadType type) {
		switch (type) {
			case SAVE: {
				if (saveThread == null) {
					return true;
				} else {
					saveThread.stopThread();
					try {
						saveThread.join(2000);
						saveThread = null;
						return true;
					} catch (InterruptedException e) {
						MessageLogger.warn("Could not stop AutoSaveThread");
						return false;
					}
				}
			}
			case BACKUP: {
				if (backupThread == null) {
					return true;
				} else {
					backupThread.stopThread();
					try {
						backupThread.join(2000);
						backupThread = null;
						return true;
					} catch (InterruptedException e) {
						MessageLogger.warn("Could not stop AutoBackupThread");
						return false;
					}
				}
			}
			case PURGE: {
				if (purgeThread == null) {
					return true;
				} else {
					purgeThread.stopThread();
					try {
						purgeThread.join(2000);
						purgeThread = null;
						return true;
					} catch (InterruptedException e) {
						MessageLogger.warn("Could not stop AutoPurgeThread");
						return false;
					}
				}
			}
			case CRASHRESTART: {
				if (crashrestartThread == null) {
					return true;
				} else {
					crashrestartThread.stopThread();
					try {
						crashrestartThread.join(2000);
						crashrestartThread = null;
						return true;
					} catch (InterruptedException e) {
						MessageLogger.warn("Could not stop CrashRestartThread");
						return false;
					}
				}
			}
			case AUTORESTART: {
				if (autorestartThread == null) {
					return true;
				} else {
					autorestartThread.stopThread();
					try {
						autorestartThread.join(2000);
						autorestartThread = null;
						return true;
					} catch (InterruptedException e) {
						MessageLogger.warn("Could not stop AutoRestartThread");
						return false;
					}
				}
			}
			case CONSOLECOMMAND: {
				if (consolecommandThread == null) {
					return true;
				} else {
					consolecommandThread.stopThread();
					try {
						consolecommandThread.join(2000);
						consolecommandThread = null;
						return true;
					} catch (InterruptedException e) {
						MessageLogger.warn("Could not stop ConsoleCommandThread");
						return false;
					}
				}
			}
			case WORLDREGENCOPY: {
				if (worldregencopyThread == null) {
					return true;
				} else {
					worldregencopyThread.stopThread();
					try {
						worldregencopyThread.join(2000);
						worldregencopyThread = null;
						return true;
					} catch (InterruptedException e) {
						MessageLogger.warn("Could not stop WorldRegenThread");
						return false;
					}
				}
			}
			case WORLDREGENPASTE: {
				if (worldregenpasteThread == null) {
					return true;
				} else {
					worldregenpasteThread.stopThread();
					try {
						worldregenpasteThread.join(2000);
						worldregenpasteThread = null;
						return true;
					} catch (InterruptedException e) {
						MessageLogger.warn("Could not stop WorldRegenThread");
						return false;
					}
				}
			}
			default: {
				return false;
			}
		}
	}

}
