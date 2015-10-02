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

package autosaveworld.threads.restart;

import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.spigotmc.AsyncCatcher;

import autosaveworld.commands.subcommands.StopCommand;
import autosaveworld.config.AutoSaveWorldConfig;
import autosaveworld.core.logging.MessageLogger;
import autosaveworld.utils.SchedulerUtils;

public class CrashRestartThread extends Thread {

	private Thread bukkitMainThread;
	private AutoSaveWorldConfig config;
	private RestartShutdownHook jvmsh;

	public CrashRestartThread(Thread thread, AutoSaveWorldConfig config, RestartShutdownHook jvmsh) {
		super("AutoSaveWorld CrashRestartThread");
		bukkitMainThread = thread;
		this.config = config;
		this.jvmsh = jvmsh;
	}

	public void stopThread() {
		run = false;
	}

	private volatile boolean run = true;

	protected long syncticktime = 0;

	@SuppressWarnings("deprecation")
	@Override
	public void run() {
		MessageLogger.debug("CrashRestartThread started");

		MessageLogger.debug("Delaying crashrestart checker start for " + config.crashRestartCheckerStartDelay + " seconds");
		// wait for configurable delay
		try {
			Thread.sleep(config.crashRestartCheckerStartDelay * 1000L);
		} catch (InterruptedException e) {
		}
		// do not enable self if plugin is disabled
		if (!run) {
			return;
		}

		MessageLogger.debug("Running crashrestart checker");
		// schedule sync task in, this will provide us info about when the last server tick occured
		SchedulerUtils.scheduleSyncRepeatingTask(new Runnable() {
			@Override
			public void run() {
				syncticktime = System.currentTimeMillis();
			}
		}, 0, 20);

		while (run) {
			long diff = System.currentTimeMillis() - syncticktime;
			if ((syncticktime != 0) && (diff >= (config.crashRestartTimeout * 1000L))) {
				run = false;

				if (config.crashRestartEnabled) {
					Logger log = Bukkit.getLogger();
					log.log(Level.SEVERE, "Server has stopped responding");
					log.log(Level.SEVERE, "Dumping threads info");
					log.log(Level.SEVERE, "Main thread");
					ArrayList<ThreadInfo> threads = new ArrayList<ThreadInfo>(Arrays.asList(ManagementFactory.getThreadMXBean().dumpAllThreads(true, true)));
					ThreadInfo mainthread = getMainThread(threads);
					dumpThread(mainthread, log);
					Plugin freezecause = find(mainthread);
					if (freezecause != null) {
						log.log(Level.SEVERE, "Freeze/crash is probably caused by plugin: "+freezecause.getName());
					}
					log.log(Level.SEVERE, "Other threads");
					for (ThreadInfo thread : threads) {
						dumpThread(thread, log);
					}
					log.log(Level.SEVERE, "Restarting Server");

					if (!config.crashRestartJustStop) {
						jvmsh.setPath(config.crashRestartScriptPath);
						Runtime.getRuntime().addShutdownHook(jvmsh);
					}

					// freeze main thread
					bukkitMainThread.suspend();
					// make sure that server won't attempt to do next tick
					StopCommand.stop();
					// disable spigot async catcher
					try {
						AsyncCatcher.enabled = false;
					} catch (Throwable t) {
					}
					// unload plugins
					Plugin[] plugins = Bukkit.getPluginManager().getPlugins();
					for (int i = plugins.length - 1; i >= 0; i--) {
						try {
							Bukkit.getPluginManager().disablePlugin(plugins[i]);
						} catch (Throwable e) {
							e.printStackTrace();
						}
					}
					// save players
					try {
						Bukkit.savePlayers();
					} catch (Throwable e) {
						e.printStackTrace();
					}
					// save worlds
					for (World w : Bukkit.getWorlds()) {
						if (w.isAutoSave()) {
							try {
								w.save();
							} catch (Throwable e) {
								e.printStackTrace();
							}
						}
					}
					// resume main thread
					bukkitMainThread.resume();
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
							// well, fuck
						}
					}

				}

			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}

		MessageLogger.debug("Graceful quit of CrashRestartThread");

	}

	private Plugin find(ThreadInfo thread) {
		HashMap<ClassLoader, Plugin> map = new HashMap<ClassLoader, Plugin>();
		for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
			map.put(plugin.getClass().getClassLoader(), plugin);
		}
		for (StackTraceElement elem : thread.getStackTrace()) {
			try {
				ClassLoader loader = Class.forName(elem.getClassName()).getClassLoader();
				if (map.containsKey(loader)) {
					return map.get(loader);
				}
			} catch (ClassNotFoundException e) {
			}
		}
		return null;
	}

	private ThreadInfo getMainThread(List<ThreadInfo> data) {
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
