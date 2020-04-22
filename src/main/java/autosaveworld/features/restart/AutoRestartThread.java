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
import java.text.SimpleDateFormat;

import autosaveworld.commands.subcommands.StopCommand;
import autosaveworld.config.AutoSaveWorldConfig;
import autosaveworld.core.AutoSaveWorld;
import autosaveworld.core.logging.MessageLogger;
import autosaveworld.utils.BukkitUtils;
import autosaveworld.utils.SchedulerUtils;
import autosaveworld.utils.Threads.SIntervalTaskThread;

public class AutoRestartThread extends SIntervalTaskThread {

	public AutoRestartThread() {
		super("AutoRestartThread");
	}

	private volatile boolean command = false;
	private volatile boolean skipcountdown = false;

	public void triggerRestart(boolean skipcountdown) {
		this.command = true;
		this.skipcountdown = skipcountdown;
	}

	@Override
	public void onStart() {
		// wait 1 minute before starting (server can restart faster than 1 minute. Without this check AutoRestartThread will stop working after restart)
		try {
			Thread.sleep(61000);
		} catch (InterruptedException e) {
		}
	}

	@Override
	public boolean isEnabled() {
		return (
			AutoSaveWorld.getInstance().getMainConfig().autoRestart &&
			AutoSaveWorld.getInstance().getMainConfig().autoRestartTimes.contains(getCurTime())
		) || command;
	}

	@Override
	public void doTask() {
		final AutoSaveWorldConfig config = AutoSaveWorld.getInstance().getMainConfig();

		stopThread();

		if (config.autoRestartCountdown && !skipcountdown) {
			for (int i = config.autoRestartCountdownSeconds.get(0); i > 0; i--) {
				if (config.autoRestartCountdownSeconds.contains(i)) {
					MessageLogger.broadcast(AutoSaveWorld.getInstance().getMessageConfig().messageAutoRestartCountdown.replace("{SECONDS}", String.valueOf(i)), true);
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}
		}

		MessageLogger.broadcast(AutoSaveWorld.getInstance().getMessageConfig().messageAutoRestart, config.autoRestartBroadcast);

		MessageLogger.debug("AutoRestarting server");

		if (!config.restartJustStop) {
			Runtime.getRuntime().addShutdownHook(new RestartShutdownHook(new File(config.autoRestartScriptPath)));
		}

		SchedulerUtils.callSyncTaskAndWait(new Runnable() {
			@Override
			public void run() {
				for (String command : config.autoRestartPreStopCommmands) {
					BukkitUtils.dispatchCommandAsConsole(command);
				}
			}
		}, 10);

		StopCommand.stop();
	}

	private final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
	private String getCurTime() {
		return sdf.format(System.currentTimeMillis());
	}

}
