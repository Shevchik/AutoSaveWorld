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

package autosaveworld.features.restart;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.spigotmc.AsyncCatcher;

import autosaveworld.commands.subcommands.StopCommand;
import autosaveworld.core.AutoSaveWorld;
import autosaveworld.core.logging.MessageLogger;
import autosaveworld.utils.SchedulerUtils;
import autosaveworld.utils.Threads.SIntervalTaskThread;
import co.aikar.timings.MinecraftTimings;

public class CrashRestartThread extends SIntervalTaskThread {

	private final Thread bukkitMainThread;
	public CrashRestartThread(Thread mainthread) {
		super("CrashRestartThread");
		this.bukkitMainThread = mainthread;
	}

	protected long syncticktime = 0;

	@Override
	protected void onStart() {
		// wait for configurable delay
		int delay = AutoSaveWorld.getInstance().getMainConfig().restartOnCrashCheckerStartDelay;
		MessageLogger.debug("Delaying crashrestart checker start for " + delay + " seconds");
		try {
			Thread.sleep(delay * 1000L);
		} catch (InterruptedException e) {
		}
		// schedule sync task in, this will provide us info about when the last server tick occured
		SchedulerUtils.scheduleSyncRepeatingTask(new Runnable() {
			@Override
			public void run() {
				syncticktime = System.currentTimeMillis();
			}
		}, 0, 20);
	}

	@Override
	public boolean isEnabled() {
		long diff = System.currentTimeMillis() - syncticktime;
		return
		(AutoSaveWorld.getInstance().getMainConfig().restartOncrashEnabled) &&
		(syncticktime != 0) &&
		(diff >= AutoSaveWorld.getInstance().getMainConfig().restartOnCrashTimeout * 1000L);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void doTask() {
		stopThread();

		Logger log = Bukkit.getLogger();
		log.log(Level.SEVERE, "Server has stopped responding");
		log.log(Level.SEVERE, "Dumping threads info");
		log.log(Level.SEVERE, "Main thread");
		ArrayList<ThreadInfo> threads = new ArrayList<ThreadInfo>(Arrays.asList(ManagementFactory.getThreadMXBean().dumpAllThreads(true, true)));
		ThreadInfo mainthread = extractMainThread(threads);
		dumpThread(mainthread, log);
		log.log(Level.SEVERE, "Other threads");
		for (ThreadInfo thread : threads) {
			dumpThread(thread, log);
		}

		if (!AutoSaveWorld.getInstance().getMainConfig().restartJustStop) {
			Runtime.getRuntime().addShutdownHook(new RestartShutdownHook(new File(AutoSaveWorld.getInstance().getMainConfig().restartOnCrashScriptPath)));
		}

		// make sure that we don't trigger restart twice
		StopCommand.stop();
		// freeze main thread
		bukkitMainThread.suspend();
		// kill main thread, so it will exit all monitors
		// will have to attempt to kill it while it is still active because plugins code may catch throwables
		while (bukkitMainThread.isAlive()) {
			bukkitMainThread.stop();
		}
		// disable spigot async catcher
		try {
			AsyncCatcher.enabled = false;
		} catch (Throwable t) {
		}
		// disable paper timings so async access doesn't print unneeded exceptions
		try {
			MinecraftTimings.stopServer();
		} catch (Throwable t) {
		}
		log.log(Level.SEVERE, "Disabling plugins");
		// unload plugins
		Plugin[] plugins = Bukkit.getPluginManager().getPlugins();
		for (int i = plugins.length - 1; i >= 0; i--) {
			try {
				log.log(Level.SEVERE, "Disabling plugin "+plugins[i].getName());
				Bukkit.getPluginManager().disablePlugin(plugins[i]);
			} catch (Throwable e) {
				log.log(Level.SEVERE, "Error while disabling plugin", e);
			}
		}
		log.log(Level.SEVERE, "Saving players");
		// save players
		try {
			Bukkit.savePlayers();
		} catch (Throwable e) {
			log.log(Level.SEVERE, "Error while saving players", e);
		}
		log.log(Level.SEVERE, "Saving worlds");
		// save worlds
		for (World w : Bukkit.getWorlds()) {
			if (w.isAutoSave()) {
				try {
					log.log(Level.SEVERE, "Saving world " + w.getName());
					w.save();
				} catch (Throwable e) {
					log.log(Level.SEVERE, "Error while saving world", e);
				}
			}
		}
		log.log(Level.SEVERE, "Restarting server");
		// shutdown JVM
		try {
			System.exit(0);
		} catch (Throwable t) {
			// fuck you forge
			try {
				Class<?> shutdownclass = Class.forName("java.lang.Shutdown", false, ClassLoader.getSystemClassLoader());
				Method shutdownmethod = shutdownclass.getDeclaredMethod("exit", int.class);
				shutdownmethod.setAccessible(true);
				shutdownmethod.invoke(null, 0);
			} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				MessageLogger.exception("Unable to shutdown JVM normally", t);
				MessageLogger.exception("Unable to shutdown JVM using workaround", e);
			}
		}
	}

	private ThreadInfo extractMainThread(List<ThreadInfo> data) {
		Iterator<ThreadInfo> it = data.iterator();
		while (it.hasNext()) {
			ThreadInfo info = it.next();
			if (info.getThreadId() == bukkitMainThread.getId()) {
				it.remove();
				return info;
			}
		}
		return null;
	}

	private void dumpThread(ThreadInfo thread, Logger log) {
		log.log(Level.SEVERE, "------------------------------");
		log.log(Level.SEVERE, "Current Thread: " + thread.getThreadName());
		log.log(Level.SEVERE, "\tPID: " + thread.getThreadId() + " | Suspended: " + thread.isSuspended() + " | Native: " + thread.isInNative() + " | State: " + thread.getThreadState());
		if (thread.getLockedMonitors().length != 0) {
			log.log(Level.SEVERE, "\tThread is waiting on monitor(s):");
			for (MonitorInfo monitor : thread.getLockedMonitors()) {
				log.log(Level.SEVERE, "\t\tLocked on:" + monitor.getLockedStackFrame());
			}
		}
		log.log(Level.SEVERE, "\tStack:");
		for (StackTraceElement stack : thread.getStackTrace()) {
			log.log(Level.SEVERE, "\t\t" + stack);
		}
		log.log(Level.SEVERE, "------------------------------");
	}

}
