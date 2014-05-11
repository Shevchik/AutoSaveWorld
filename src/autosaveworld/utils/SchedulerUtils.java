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

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;

import autosaveworld.core.AutoSaveWorld;

public class SchedulerUtils {

	private static AutoSaveWorld plugin;

	public static void init(AutoSaveWorld plugin) {
		SchedulerUtils.plugin = plugin;
	}

	public static void callSyncTaskAndWait(Runnable run) {
		scheduleSyncTaskAndWaitInternal(run, 0);
	}

	public static void callSyncTaskAndWait(Runnable run, int timeout) {
		scheduleSyncTaskAndWaitInternal(run, timeout);
	}

	private static void scheduleSyncTaskAndWaitInternal(final Runnable run, int timeout) {
		final LinkedBlockingQueue<Boolean> lock = new LinkedBlockingQueue<Boolean>(1);
		Bukkit.getScheduler().scheduleSyncDelayedTask(
			plugin,
			new Runnable() {
				@Override
				public void run() {
					run.run();
					lock.offer(true);
				}
			}
		);
		try {
			if (timeout == 0) {
				lock.take();
			} else {
				lock.poll(timeout, TimeUnit.SECONDS);
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
