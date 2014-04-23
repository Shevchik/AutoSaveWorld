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

	private static ThreadLocal<Boolean> syncTaskFinishedFlag = new ThreadLocal<Boolean>() {
		@Override
		protected Boolean initialValue() {
			return false;
		}
	};
	public static void callSyncTaskAndWait(final Runnable run) {
		if (Bukkit.isPrimaryThread()) {
			throw new RuntimeException("This method can't be called from the main thread");
		}
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,
			new Runnable() {
				@Override
				public void run() {
					run.run();
					syncTaskFinishedFlag.set(true);
				}
			}
		);
		while (!syncTaskFinishedFlag.get()) {
			try {Thread.sleep(50);} catch (InterruptedException e) {e.printStackTrace();}
		}
	}

	public static void scheduleSyncTask(Runnable run) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, run);
	}

	public static void scheduleSyncTask(Runnable run, int delay) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, run, delay);
	}

}
