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

package autosaveworld;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class AutoSaveWorld extends JavaPlugin {
	private static final Logger log = Bukkit.getLogger();

	
	public AutoSaveThread saveThread = null;
	public AutoBackupThread backupThread6 = null;
	public AutoPurgeThread purgeThread = null;
	public SelfRestartThread selfrestartThread = null;
	public CrashRestartThread crashrestartThread = null;
	public AutoRestartThread autorestartThread = null;
	public JVMshutdownhook JVMsh = null;
	private AutoSaveConfigMSG configmsg;
	private AutoSaveConfig config;
	private LocaleContainer localeloader;
	private ASWEventListener eh;
	protected int numPlayers = 0;
	protected volatile boolean saveInProgress = false;
	protected volatile boolean backupInProgress = false;
	protected volatile boolean purgeInProgress = false;
	protected String LastSave = "No save was since the server start";
	protected String LastBackup = "No backup was since the server start";

	@Override
	public void onDisable() {
		// Perform a Save NOW!
		saveThread.command = true;
		saveThread.performSave();
		// Stop threads
		debug("Stopping Threads");
		stopThread(ThreadType.SAVE);
		stopThread(ThreadType.BACKUP6);
		stopThread(ThreadType.PURGE);
		if (!selfrestartThread.restart) {
			stopThread(ThreadType.SELFRESTART);
			log.info("[AutoSaveWorld] Graceful quit of selfrestart thread");
		}
		stopThread(ThreadType.CRASHRESTART);
		stopThread(ThreadType.AUTORESTART);
		JVMsh = null;
		log.info(String.format("[%s] Version %s is disabled", getDescription()
				.getName(), getDescription().getVersion()));
	}

	@Override
	public void onEnable() {
		// Load Configuration
		config = new AutoSaveConfig();
		config.load();
		configmsg = new AutoSaveConfigMSG(config);
		configmsg.loadmsg();
		localeloader = new LocaleContainer(this, config, configmsg);
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
		startThread(ThreadType.BACKUP6);
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
		// Notify on logger load
		log.info(String.format("[%s] Version %s is enabled: %s",
				getDescription().getName(), getDescription().getVersion(),
				config.varUuid.toString()));
	}

	
	
	
	protected boolean startThread(ThreadType type) {
		switch (type) {
		case SAVE:
			if (saveThread == null || !saveThread.isAlive()) {
				saveThread = new AutoSaveThread(this, config, configmsg);
				saveThread.start();
			}
			return true;
		case BACKUP6:
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
		case SELFRESTART:
			if (selfrestartThread == null || !selfrestartThread.isAlive()) {
				selfrestartThread = new SelfRestartThread(this);
				selfrestartThread.start();
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
				saveThread.setRun(false);
				try {
					saveThread.join(1000);
					saveThread = null;
					return true;
				} catch (InterruptedException e) {
					warn("Could not stop AutoSaveThread");
					return false;
				}
			}
		case BACKUP6:
			if (backupThread6 == null) {
				return true;
			} else {
				backupThread6.setRun(false);
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
				purgeThread.setRun(false);
				try {
					purgeThread.join(1000);
					purgeThread = null;
					return true;
				} catch (InterruptedException e) {
					warn("Could not stop AutoPurgeThread");
					return false;
				}
			}
		case SELFRESTART:
			if (selfrestartThread == null) {
				return true;
			} else {
				selfrestartThread.stopthread();
				try {
					selfrestartThread.join(1000);
					selfrestartThread = null;
					return true;
				} catch (InterruptedException e) {
					warn("Could not stop SelfRestartThread");
					return false;
				}
			}
		case CRASHRESTART:
			if (crashrestartThread == null) {
				return true;
			} else {
				crashrestartThread.stopthread();
				try {
					crashrestartThread.join(1000);
					crashrestartThread = null;
					return true;
				} catch (InterruptedException e) {
					warn("Could not stop SelfRestartThread");
					return false;
				}
			}
		case AUTORESTART:
			if (autorestartThread == null) {
				return true;
			} else {
				autorestartThread.stopthread();
				try {
					autorestartThread.join(1000);
					autorestartThread = null;
					return true;
				} catch (InterruptedException e) {
					warn("Could not stop SelfRestartThread");
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
