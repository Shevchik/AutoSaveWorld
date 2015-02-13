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

package autosaveworld.threads;

import autosaveworld.core.logging.MessageLogger;

public abstract class IntervalTaskThread extends Thread {

	public IntervalTaskThread(String threadname) {
		super("AutoSaveWorld "+threadname);
	}

	private volatile boolean run = true;
	private volatile boolean runnow = false;

	public void triggerTaskRun() {
		runnow = true;
	}

	public boolean isRunning() {
		return run;
	}

	public void stopThread() {
		run = false;
	}

	private int currentTick = 0;

	@Override
	public void run() {
		MessageLogger.debug(getName()+" started");
		onStart();
		while (run) {
			int interval = getInterval();
			if (currentTick > interval || runnow) {
				runnow = false;
				currentTick = 0;
				if (isEnabled()) {
					try {
						doTask();
					} catch (Throwable t) {
						t.printStackTrace();
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

	protected void onStart() {
	}

	protected void onStop() {
	}

	public abstract boolean isEnabled();

	public abstract int getInterval();

	public abstract void doTask();

}
