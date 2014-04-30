/**
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */

package autosaveworld.utils;

import org.bukkit.Bukkit;

import autosaveworld.core.AutoSaveWorld;

public class SchedulerUtils {

	private static AutoSaveWorld plugin;
	public SchedulerUtils(AutoSaveWorld plugin) {
		SchedulerUtils.plugin = plugin;
	}

	public static void callSyncTaskAndWait(Runnable run) {
		scheduleSyncTaskAndWaitInternal(run, 0);
	}

	public static void callSyncTaskAndWait(Runnable run, int timeout) {
		scheduleSyncTaskAndWaitInternal(run, timeout *= 1000);
	}

	private static void scheduleSyncTaskAndWaitInternal(final Runnable run, int timeout) {
		if (Bukkit.isPrimaryThread()) {
			throw new RuntimeException("This method can't be called from the main thread");
		}
		final Object lock = new Object();
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,
			new Runnable() {
				@Override
				public void run() {
					run.run();
					synchronized (lock) {
						lock.notify();
					}
				}
			}
		);
		try {
			synchronized (lock) {
				lock.wait(timeout);
			}
		} catch (InterruptedException e) {
		}
	}

	public static void scheduleSyncTask(Runnable run) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, run);
	}

	public static void scheduleSyncRepeatingTask(Runnable run, int delay, int interval) {
		Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, run, delay, interval);
	}

}
