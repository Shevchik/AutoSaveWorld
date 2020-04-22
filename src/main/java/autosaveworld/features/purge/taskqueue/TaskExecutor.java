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

package autosaveworld.features.purge.taskqueue;

import java.util.ArrayList;

import autosaveworld.utils.SchedulerUtils;

public class TaskExecutor implements AutoCloseable {

	private int tasksLimit;

	public TaskExecutor(int tasksLimit) {
		this.tasksLimit = tasksLimit;
	}

	protected final ArrayList<Task> tasks = new ArrayList<Task>();

	public void execute(final Task task) {
		if (task.doNotQueue()) {
			SchedulerUtils.callSyncTaskAndWait(
				new Runnable() {
					@Override
					public void run() {
						task.performTask();
					}
				}
			);
		} else {
			tasks.add(task);
			if (tasks.size() >= tasksLimit) {
				flush();
			}
		}
	}

	protected void flush() {
		SchedulerUtils.callSyncTaskAndWait(
			new Runnable() {
				@Override
				public void run() {
					for (Task task : tasks) {
						task.performTask();
					}
					tasks.clear();
				}
			}
		);
	}

	@Override
	public void close() {
		flush();
	}

}
