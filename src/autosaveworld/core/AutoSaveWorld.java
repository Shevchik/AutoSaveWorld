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

import java.util.logging.Logger;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.plugin.java.JavaPlugin;

import autosaveworld.commands.CommandsHandler;
import autosaveworld.config.AutoSaveConfig;
import autosaveworld.config.AutoSaveConfigMSG;
import autosaveworld.config.LocaleChanger;
import autosaveworld.listener.EventsListener;
import autosaveworld.pluginmanager.ASWPluginManager;
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

public class AutoSaveWorld extends JavaPlugin {

	private Logger log;
	private FormattingCodesParser formattingCodesParser = new FormattingCodesParser();

	//constatns
	public GlobalConstants constants = null;
	//save
	public AutoSaveThread saveThread = null;
	//backup
	public AutoBackupThread backupThread6 = null;
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
	public ASWPluginManager pmanager;
	//configs
	public AutoSaveConfigMSG configmsg;
	public AutoSaveConfig config;
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
			warn("Other autosaveworld operation is in progress, current operation aborted");
			return false;
		}
		return true;
	}

	@Override
	public void onEnable() {
		// Load Configuration
		log = getLogger();
		constants = new GlobalConstants(this);
		config = new AutoSaveConfig(this);
		config.load();
		configmsg = new AutoSaveConfigMSG(this);
		configmsg.loadmsg();
		localeChanger = new LocaleChanger(this, configmsg);
		eh = new EventsListener(this,config);
		ch = new CommandsHandler(this,config,configmsg,localeChanger);
		//load plugin manager
		pmanager = new ASWPluginManager(this);
		// register events and commands
		getCommand("autosaveworld").setExecutor(ch);
		getCommand("autosave").setExecutor(ch);
		getCommand("autobackup").setExecutor(ch);
		getCommand("autopurge").setExecutor(ch);
		getServer().getPluginManager().registerEvents(eh, this);
		// Start AutoSave Thread
		startThread(ThreadType.SAVE);
		// Start AutoBackupThread
		startThread(ThreadType.BACKUP);
		// Start AutoPurgeThread
		startThread(ThreadType.PURGE);
		// Create JVMsh
		JVMsh = new RestartJVMshutdownhook();
		// Start CrashRestartThread
		startThread(ThreadType.CRASHRESTART);
		// Start AutoRestartThread
		startThread(ThreadType.AUTORESTART);
		// Start ConsoleCommandThread
		startThread(ThreadType.CONSOLECOMMAND);
		// Start WorldRegenCopyThread
		startThread(ThreadType.WORLDREGENCOPY);
		// Start WorldRegenPasteThread
		startThread(ThreadType.WORLDREGENPASTE);
	}


	@Override
	public void onDisable() {
		if (config.saveOnASWDisable) {
			// Perform a Save NOW!
			debug("Saving");
			saveThread.performSaveNow();
		}
		// Save config
		debug("Saving config");
		config.save();
		// Stop threads
		debug("Stopping Threads");
		stopThread(ThreadType.SAVE);
		stopThread(ThreadType.BACKUP);
		stopThread(ThreadType.PURGE);
		stopThread(ThreadType.CRASHRESTART);
		stopThread(ThreadType.AUTORESTART);
		JVMsh = null;
		stopThread(ThreadType.CONSOLECOMMAND);
		stopThread(ThreadType.WORLDREGENCOPY);
		//worldregenpaste will stop itself at server start if it don't need to paste something, so we should not care about stopping this thread.
		worldregenpasteThread = null;
		//null plugin manager
		pmanager = null;
		//null values
		configmsg = null;
		config = null;
		localeChanger = null;
		eh = null;
		ch = null;
		formattingCodesParser = null;
		constants = null;
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
				if (backupThread6 == null || !backupThread6.isAlive()) {
					backupThread6 = new AutoBackupThread(this, config, configmsg);
					backupThread6.start();
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
					crashrestartThread = new CrashRestartThread(this, config, JVMsh);
					crashrestartThread.start();
				}
				return true;
			}
			case AUTORESTART: {
				if (autorestartThread == null || !autorestartThread.isAlive()) {
					autorestartThread = new AutoRestartThread(this, config, configmsg, JVMsh);
					autorestartThread.start();
				}
				return true;
			}
			case CONSOLECOMMAND: {
				if (consolecommandThread == null || !consolecommandThread.isAlive()) {
					consolecommandThread = new AutoConsoleCommandThread(this, config);
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
						warn("Could not stop AutoSaveThread");
						return false;
					}
				}
			}
			case BACKUP: {
				if (backupThread6 == null) {
					return true;
				} else {
					backupThread6.stopThread();
					try {
						backupThread6.join(2000);
						backupThread6 = null;
						return true;
					} catch (InterruptedException e) {
						warn("Could not stop AutoBackupThread");
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
						warn("Could not stop AutoPurgeThread");
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
						warn("Could not stop CrashRestartThread");
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
						warn("Could not stop AutoRestartThread");
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
						warn("Could not stop ConsoleCommandThread");
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
						warn("Could not stop WorldRegenThread");
						return false;
					}
				}
			}
			default: {
				return false;
			}
		}
	}

	public void sendMessage(CommandSender sender, String message) {
		if (!message.equals("")) {
			if (formattingCodesParser != null) {
				sender.sendMessage(formattingCodesParser.parseFormattingCodes(message));
			}
		}
	}

	public void broadcast(String message, boolean broadcast) {
		if (!message.equals("") && broadcast) {
			if (formattingCodesParser != null) {
				getServer().broadcastMessage(formattingCodesParser.parseFormattingCodes(message));
			}
		}
	}

	public void kickPlayer(Player player, String message) {
		if (formattingCodesParser != null) {
			player.kickPlayer(formattingCodesParser.parseFormattingCodes(message));
		}
	}

	public void disallow(PlayerLoginEvent e, String message) {
		if (formattingCodesParser != null) {
			e.disallow(Result.KICK_OTHER, formattingCodesParser.parseFormattingCodes(message));
		}
	}

	public void debug(String message) {
		if (config != null && config.varDebug) {
			if (formattingCodesParser != null) {
				log.info(formattingCodesParser.stripFormattingCodes(message));
			}
		}
	}

	public void warn(String message) {
		if (log != null) {
			if (formattingCodesParser != null) {
				log.warning(formattingCodesParser.stripFormattingCodes(message));
			}
		}
	}

}
