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

import java.text.SimpleDateFormat;

import autosaveworld.commands.subcommands.StopCommand;
import autosaveworld.config.AutoSaveWorldConfig;
import autosaveworld.config.AutoSaveWorldConfigMSG;
import autosaveworld.core.logging.MessageLogger;
import autosaveworld.utils.CommandUtils;
import autosaveworld.utils.SchedulerUtils;

public class AutoRestartThread extends Thread {

	private AutoSaveWorldConfig config;
	private AutoSaveWorldConfigMSG configmsg;
	private RestartShutdownHook jvmsh;

	public AutoRestartThread(AutoSaveWorldConfig config, AutoSaveWorldConfigMSG configmsg, RestartShutdownHook jvmsh) {
		this.config = config;
		this.configmsg = configmsg;
		this.jvmsh = jvmsh;
	}

	public void stopThread() {
		run = false;
	}

	public void startrestart(boolean skipcountdown) {
		command = true;
		this.skipcountdown = skipcountdown;
	}

	private volatile boolean run = true;
	private volatile boolean command = false;
	private volatile boolean skipcountdown = false;

	@Override
	public void run() {
		MessageLogger.debug("AutoRestartThread started");
		Thread.currentThread().setName("AutoSaveWorld AutoRestartThread");

		// check if we just restarted (server can restart faster than 1 minute. Without this check AutoRestartThread will stop working after restart)
		if (config.autoRestartTimes.contains(getCurTime())) {
			try {
				Thread.sleep(61000);
			} catch (InterruptedException e) {
			}
		}

		while (run) {
			if ((config.autoRestart && config.autoRestartTimes.contains(getCurTime())) || command) {
				run = false;
				command = false;

				if (config.autoRestartCountdown && !skipcountdown) {
					for (int i = config.autoRestartCountdownSeconds.get(0); i > 0; i--) {
						if (config.autoRestartCountdownSeconds.contains(i)) {
							MessageLogger.broadcast(configmsg.messageAutoRestartCountdown.replace("{SECONDS}", String.valueOf(i)), true);
						}
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
						}
					}
				}

				MessageLogger.broadcast(configmsg.messageAutoRestart, config.autoRestartBroadcast);

				MessageLogger.debug("AutoRestarting server");

				if (!config.restartJustStop) {
					jvmsh.setPath(config.autoRestartScriptPath);
					Runtime.getRuntime().addShutdownHook(jvmsh);
				}

				SchedulerUtils.callSyncTaskAndWait(new Runnable() {
					@Override
					public void run() {
						for (String command : config.autoRestartPreStopCommmands) {
							CommandUtils.dispatchCommandAsConsole(command);
						}
					}
				}, 10);

				StopCommand.stop();

			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}

		MessageLogger.debug("Graceful quit of AutoRestartThread");

	}

	private final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
	private String getCurTime() {
		return sdf.format(System.currentTimeMillis());
	}

}
