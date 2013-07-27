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

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import autosaveworld.config.AutoSaveConfig;
import autosaveworld.config.AutoSaveConfigMSG;
import autosaveworld.config.LocaleLoader;
import autosaveworld.listener.ASWEventListener;
import autosaveworld.threads.backup.AutoBackupThread;
import autosaveworld.threads.consolecommand.AutoConsoleCommandThread;
import autosaveworld.threads.purge.AutoPurgeThread;
import autosaveworld.threads.restart.AutoRestartThread;
import autosaveworld.threads.restart.CrashRestartThread;
import autosaveworld.threads.restart.JVMshutdownhook;
import autosaveworld.threads.save.AutoSaveThread;
import autosaveworld.threads.worldregen.AntiJoinListener;
import autosaveworld.threads.worldregen.WorldRegenPasteThread;
import autosaveworld.threads.worldregen.WorldRegenCopyThread;

public class AutoSaveWorld extends JavaPlugin {
	private static final Logger log = Bukkit.getLogger();

	
	public AutoSaveThread saveThread = null;
	public AutoBackupThread backupThread6 = null;
	public AutoPurgeThread purgeThread = null;
	public CrashRestartThread crashrestartThread = null;
	public AutoRestartThread autorestartThread = null;
	public JVMshutdownhook JVMsh = null;
	public AutoConsoleCommandThread consolecommandThread = null;
	public WorldRegenCopyThread worldregenThread = null;
	public WorldRegenPasteThread wrp = null;
	public boolean worldregenfinished = false;
	public AutoSaveConfigMSG configmsg;
	public AutoSaveConfig config;
	public LocaleLoader localeloader;
	public ASWEventListener eh;
	public AntiJoinListener ajl;
	public volatile boolean saveInProgress = false;
	public volatile boolean backupInProgress = false;
	public volatile boolean purgeInProgress = false;
	public String LastSave = "No save was since the server start";
	public String LastBackup = "No backup was since the server start";

	
	@Override
	public void onEnable() {
		// Load Configuration
		config = new AutoSaveConfig();
		config.load();
		configmsg = new AutoSaveConfigMSG(config);
		configmsg.loadmsg();
		localeloader = new LocaleLoader(this, config, configmsg);
		eh = new ASWEventListener(this, config, configmsg, localeloader);
		// register events and commands
		getCommand("autosaveworld").setExecutor(eh);
		getCommand("autosave").setExecutor(eh);
		getCommand("autobackup").setExecutor(eh);
		getCommand("autopurge").setExecutor(eh);
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
		JVMsh = new JVMshutdownhook();
		// Start ConsoleCommandThread
		startThread(ThreadType.CONSOLECOMMAND);
		// Start WorldRegenThread
		startThread(ThreadType.WORLDREGEN);
		//Check if we are in WOrldRegen stage 3, if so - do our job
		File check = new File("plugins/AutoSaveWorld/WorldRegenTemp/shouldpaste");
		if (check.exists()) {
			ajl = new AntiJoinListener(this,configmsg);
			getServer().getPluginManager().registerEvents(ajl, this);
			wrp = new WorldRegenPasteThread(this,config,configmsg);
			wrp.start();
		}
		// Notify on logger load
		log.info(String.format("[%s] Version %s is enabled",
					getDescription().getName(), getDescription().getVersion()
					)
				);
	}
	
	
	@Override
	public void onDisable() {
		// Perform a Save NOW!
		saveThread.command = true;
		saveThread.performSave();
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
		HandlerList.unregisterAll(this);
		//Check if we just finished WorldRegen, if so - clean garbage
		File check = new File("plugins/AutoSaveWorld/WorldRegenTemp/shouldpaste");
		if (check.exists() && worldregenfinished) {
			PlayerJoinEvent.getHandlerList().unregister(ajl);
			wrp = null;
			check.delete();
			new File("plugins/AutoSaveWorld/WorldRegenTemp/wname.yml").delete();
			new File("plugins/AutoSaveWorld/WorldRegenTemp/").delete();
		}
		log.info(String.format("[%s] Version %s is disabled", getDescription()
				.getName(), getDescription().getVersion()));
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
				worldregenThread = new WorldRegenCopyThread(this, config ,configmsg);
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
					saveThread.join(1000);
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
					backupThread6.join(1000);
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
					purgeThread.join(1000);
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
					crashrestartThread.join(1000);
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
					autorestartThread.join(1000);
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
					consolecommandThread.join(1000);
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
					worldregenThread.join(1000);
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

	
	
	
	
	public void sendMessage(CommandSender sender, String message) {
		if (!message.equals("")) {
			sender.sendMessage(Generic.parseColor(message));
		}
	}

	public void broadcast(String message) {
		if (!message.equals("")) {
			getServer().broadcastMessage(Generic.parseColor(message));
			log.info(String.format("[%s] %s", getDescription().getName(),
					Generic.stripColor(message)));
		}

	}
	
	public void kickPlayer(Player player, String message)
	{
		player.kickPlayer(Generic.parseColor(message));
	}

	public void debug(String message) {
		if (config.varDebug) {
			log.info(String.format("[%s] %s", getDescription().getName(),
					Generic.stripColor(message)));
		}
	}

	public void warn(String message) {
		log.warning(String.format("[%s] %s", getDescription().getName(),
				Generic.stripColor(message)));
	}

}
