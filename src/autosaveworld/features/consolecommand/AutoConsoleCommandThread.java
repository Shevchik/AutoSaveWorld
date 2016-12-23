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

package autosaveworld.features.consolecommand;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import autosaveworld.core.AutoSaveWorld;
import autosaveworld.core.logging.MessageLogger;
import autosaveworld.utils.BukkitUtils;
import autosaveworld.utils.SchedulerUtils;
import autosaveworld.utils.Threads.SIntervalTaskThread;

public class AutoConsoleCommandThread extends SIntervalTaskThread {

	public AutoConsoleCommandThread() {
		super("AutoConsoleCommandThread");
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void doTask() {
		// handle times mode
		if (AutoSaveWorld.getInstance().getMainConfig().ccTimesModeEnabled) {
			for (String ctime : getTimesToExecute()) {
				MessageLogger.debug("Executing console commands (timesmode)");
				executeCommands(AutoSaveWorld.getInstance().getMainConfig().ccTimesModeCommands.get(ctime));
			}
		}

		// handle interval mode
		if (AutoSaveWorld.getInstance().getMainConfig().ccIntervalsModeEnabled) {
			for (int interval : getIntervalsToExecute()) {
				MessageLogger.debug("Executing console commands (intervalmode)");
				executeCommands(AutoSaveWorld.getInstance().getMainConfig().ccIntervalsModeCommands.get(interval));
			}
		}
	}

	private void executeCommands(final List<String> commands) {
		if (isEnabled()) {
			SchedulerUtils.scheduleSyncTask(new Runnable() {
				@Override
				public void run() {
					for (String command : commands) {
						BukkitUtils.dispatchCommandAsConsole(command);
					}
				}
			});
		}
	}

	// timesmode checks
	private int minute = -1;
	private final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
	private final SimpleDateFormat msdf = new SimpleDateFormat("mm");

	private List<String> getTimesToExecute() {
		List<String> timestoexecute = new ArrayList<String>();
		int cminute = Integer.parseInt(msdf.format(System.currentTimeMillis()));
		String ctime = sdf.format(System.currentTimeMillis());
		if ((cminute != minute) && AutoSaveWorld.getInstance().getMainConfig().ccTimesModeCommands.containsKey(ctime)) {
			minute = cminute;
			timestoexecute.add(ctime);
		}
		return timestoexecute;
	}

	// intervalmode checks
	private long intervalcounter = 0;

	private List<Integer> getIntervalsToExecute() {
		List<Integer> inttoexecute = new ArrayList<Integer>();
		for (int interval : AutoSaveWorld.getInstance().getMainConfig().ccIntervalsModeCommands.keySet()) {
			if ((intervalcounter != 0) && ((intervalcounter % interval) == 0)) {
				inttoexecute.add(interval);
			}
		}
		intervalcounter++;
		return inttoexecute;
	}

}