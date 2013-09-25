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
import java.util.logging.Logger;

import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import autosaveworld.commands.CommandsHandler;
import autosaveworld.config.AutoSaveConfig;
import autosaveworld.config.AutoSaveConfigMSG;
import autosaveworld.config.LocaleLoader;
import autosaveworld.listener.EventsListener;
import autosaveworld.pluginmanager.ASWPluginManager;
import autosaveworld.threads.RestartWaiter;
import autosaveworld.threads.ThreadType;
import autosaveworld.threads.backup.AutoBackupThread;
import autosaveworld.threads.consolecommand.AutoConsoleCommandThread;
import autosaveworld.threads.purge.AutoPurgeThread;
import autosaveworld.threads.restart.AutoRestartThread;
import autosaveworld.threads.restart.CrashRestartThread;
import autosaveworld.threads.restart.JVMshutdownhook;
import autosaveworld.threads.save.AutoSaveThread;
import autosaveworld.threads.worldregen.WorldRegenPasteThread;
import autosaveworld.threads.worldregen.WorldRegenCopyThread;

public class AutoSaveWorld extends JavaPlugin {
	
	private Logger log;
	private FormattingCodesParser formattingCodesParser = new FormattingCodesParser(); 

	//constatns
	public Constants constants = null;
	//save
	public AutoSaveThread saveThread = null;
	//backup
	public AutoBackupThread backupThread6 = null;
	//purge
	public AutoPurgeThread purgeThread = null;
	//restart
	public CrashRestartThread crashrestartThread = null;
	public AutoRestartThread autorestartThread = null;
	public JVMshutdownhook JVMsh = null;
	//autoconsolecommand
	public AutoConsoleCommandThread consolecommandThread = null;
	//worldregen
	public WorldRegenCopyThread worldregenThread = null;
	public WorldRegenPasteThread wrp = null;
	public volatile boolean worldregenfinished = false;
	//global restart allowed waiter
	public RestartWaiter restartwaiter;
	//plugin manager
	public ASWPluginManager pmanager;
	//configs
	public AutoSaveConfigMSG configmsg;
	public AutoSaveConfig config;
	public LocaleLoader localeloader;
	//event listener
	public EventsListener eh;
	//command executor
	public CommandsHandler ch;
	//locks
	public volatile boolean saveInProgress = false;
	public volatile boolean backupInProgress = false;
	public volatile boolean purgeInProgress = false;
	public volatile boolean worldregenInProcess = false;
	//info
	public String LastSave = "No save was since the server start";
	public String LastBackup = "No backup was since the server start";

	
	@Override
	public void onEnable() {
		// Load Configuration
		log = getLogger();
		constants = new Constants(this);
		config = new AutoSaveConfig(this);
		config.load();
		configmsg = new AutoSaveConfigMSG(this,config);
		configmsg.loadmsg();
		localeloader = new LocaleLoader(this, config, configmsg);
		eh = new EventsListener(this);
		ch = new CommandsHandler(this,config,configmsg,localeloader);
		//load plugin manager
		pmanager = new ASWPluginManager(this);
		//load restart waiter
		restartwaiter = new RestartWaiter();
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
		// Start SelfRestarThread
		startThread(ThreadType.SELFRESTART);
		// Start CrashRestartThread
		startThread(ThreadType.CRASHRESTART);
		// Start AutoRestartThread
		startThread(ThreadType.AUTORESTART);
		// Create JVMsh
		JVMsh = new JVMshutdownhook(restartwaiter);
		// Start ConsoleCommandThread
		startThread(ThreadType.CONSOLECOMMAND);
		// Start WorldRegenThread
		startThread(ThreadType.WORLDREGEN);
		//Check if we are in WorldRegen stage 3, if so - do our job
		File check = new File(constants.getShouldpasteFile());
		if (check.exists()) {
			worldregenInProcess = true;
			wrp = new WorldRegenPasteThread(this,config, configmsg);
			wrp.start();
		}
	}
	
	
	@Override
	public void onDisable() {
		debug("Saving");
		// Perform a Save NOW!
		performSaveOnDisable();
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
		stopThread(ThreadType.WORLDREGEN);
		configmsg = null;
		config = null; 
		localeloader = null;
		eh = null;
		ch = null;
		formattingCodesParser = null;
		pmanager = null;
		HandlerList.unregisterAll(this);
		//Check if we just finished WorldRegen, if so - clean garbage
		File check = new File(constants.getShouldpasteFile());
		if (check.exists() && worldregenfinished) {
			wrp = null;
			check.delete();
			new File(constants.getWorldnameFile()).delete();
			new File(constants.getWorldRegenTempFolder()).delete();
		}
		constants = null;
		restartwaiter = null;
	}	
	
	

	protected boolean startThread(ThreadType type) {
		switch (type) {
		case SAVE:
			if (saveThread == null || !saveThread.isAlive()) {
				saveThread = new AutoSaveThread(this, config, configmsg);
				saveThread.start();
			}
			return true;
		case BACKUP:
			if (backupThread6 == null || !backupThread6.isAlive()) {
				backupThread6 = new AutoBackupThread(this, config, configmsg);
				backupThread6.start();
			}
			return true;
		case PURGE:
			if (purgeThread == null || !purgeThread.isAlive()) {
				purgeThread = new AutoPurgeThread(this, config, configmsg);
				purgeThread.start();
			}
			return true;
		case CRASHRESTART:
			if (crashrestartThread == null || !crashrestartThread.isAlive()) {
				crashrestartThread = new CrashRestartThread(this, config);
				crashrestartThread.start();
			}
			return true;
		case AUTORESTART:
			if (autorestartThread == null || !autorestartThread.isAlive()) {
				autorestartThread = new AutoRestartThread(this, config,
						configmsg);
				autorestartThread.start();
			}
			return true;
		case CONSOLECOMMAND:
			if (consolecommandThread == null || !consolecommandThread.isAlive()) {
				consolecommandThread = new AutoConsoleCommandThread(this, config);
				consolecommandThread.start();
			}
			return true;
		case WORLDREGEN:
			if (worldregenThread == null || !worldregenThread.isAlive()) {
				worldregenThread = new WorldRegenCopyThread(restartwaiter, this, config ,configmsg);
				worldregenThread.start();
			}
			return true;
		default:
			return false;
		}
	}

	
	
	protected boolean stopThread(ThreadType type) {
		switch (type) {
		case SAVE:
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
		case BACKUP:
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
		case PURGE:
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
		case CRASHRESTART:
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
		case AUTORESTART:
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
		case CONSOLECOMMAND:
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
		case WORLDREGEN:
			if (worldregenThread == null) {
				return true;
			} else {
				worldregenThread.stopThread();
				try {
					worldregenThread.join(2000);
					worldregenThread = null;
					return true;
				} catch (InterruptedException e) {
					warn("Could not stop WorldRegenThread");
					return false;
				}
			}
		default:
			return false;
		}
	}

	private void performSaveOnDisable()
	{
		broadcast(configmsg.messageSaveBroadcastPre, config.saveBroadcast);
		debug("Saving players");
		getServer().savePlayers();
		debug("Saved Players");
		debug("Saving worlds");
		for (World w : getServer().getWorlds())
		{
			debug(String.format("Saving world: %s", w.getName()));
			w.save();
		}
		debug("Saved Worlds");
		broadcast(configmsg.messageSaveBroadcastPost, config.saveBroadcast);
	}
	
	
	
	public void sendMessage(CommandSender sender, String message) {
		if (!message.equals("")) {
			sender.sendMessage(formattingCodesParser.parseFormattingCodes(message));
		}
	}

	public void broadcast(String message, boolean broadcast) {
		if (!message.equals("") && broadcast) {
			getServer().broadcastMessage(formattingCodesParser.parseFormattingCodes(message));
		}
	}
	
	public void kickPlayer(Player player, String message) {
		player.kickPlayer(formattingCodesParser.parseFormattingCodes(message));
	}

	public void debug(String message) {
		if (config.varDebug) {
			log.info(formattingCodesParser.stripFormattingCodes(message));
		}
	}

	public void warn(String message) {
		log.warning(formattingCodesParser.stripFormattingCodes(message));
	}

}
