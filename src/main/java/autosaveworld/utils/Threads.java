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

import autosaveworld.core.AutoSaveWorld;
import autosaveworld.core.logging.MessageLogger;

public class Threads {

	public static abstract class SIntervalTaskThread extends Thread {

		public SIntervalTaskThread(String threadname) {
			super(AutoSaveWorld.getInstance().getName() + " " + threadname);
		}

		protected volatile boolean run = true;

		public boolean isRunning() {
			return run;
		}

		public void stopThread() {
			run = false;
		}

		@Override
		public void run() {
			MessageLogger.debug(getName()+" started");
			onStart();
			while (run) {
				if (isEnabled()) {
					try {
						doTask();
					} catch (Throwable t) {
						MessageLogger.exception("Exception while performing interval task", t);
						if (t instanceof ThreadDeath) {
							ReflectionUtils.throwException(t);
						}
					}
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}
			onStop();
			MessageLogger.debug(getName()+" stopped");
		}

		protected void onStart() {
		}

		protected void onStop() {
		}

		public abstract boolean isEnabled();

		public abstract void doTask() throws Exception;

	}

	public static abstract class IntervalTaskThread extends SIntervalTaskThread {

		public IntervalTaskThread(String threadname) {
			super(threadname);
		}

		private int currentTick = 0;

		protected volatile boolean runnow = false;

		public void triggerTaskRun() {
			runnow = true;
		}

		@Override
		public void run() {
			MessageLogger.debug(getName()+" started");
			onStart();
			while (run) {
				int interval = getInterval();
				boolean shouldrun = runnow;
				if ((currentTick > interval) || shouldrun) {
					runnow = false;
					currentTick = 0;
					if (isEnabled() || shouldrun) {
						try {
							doTask();
						} catch (Exception t) {
							MessageLogger.exception("Exception while performing interval task", t);
						}
					}
				}
				currentTick++;
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}
			onStop();
			MessageLogger.debug(getName()+" stopped");
		}

		public abstract int getInterval();

	}

}
