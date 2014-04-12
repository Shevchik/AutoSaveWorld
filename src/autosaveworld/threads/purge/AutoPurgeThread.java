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

package autosaveworld.threads.purge;

import autosaveworld.config.AutoSaveWorldConfig;
import autosaveworld.config.AutoSaveConfigMSG;
import autosaveworld.core.AutoSaveWorld;
import autosaveworld.threads.purge.UniquePlayerIdentifierDetector.UniquePlayerIdentifierType;
import autosaveworld.threads.purge.bynames.PurgeByNames;

public class AutoPurgeThread extends Thread {

	private AutoSaveWorld plugin = null;
	private AutoSaveWorldConfig config;
	private AutoSaveConfigMSG configmsg;
	public AutoPurgeThread(AutoSaveWorld plugin, AutoSaveWorldConfig config, AutoSaveConfigMSG configmsg) {
		this.plugin = plugin;
		this.config = config;
		this.configmsg = configmsg;
	}


	public void stopThread() {
		this.run = false;
	}

	public void startpurge() {
		command = true;
	}

	// The code to run...weee
	private volatile boolean run = true;
	private boolean command = false;
	@Override
	public void run() {

		plugin.debug("AutoPurgeThread Started");
		Thread.currentThread().setName("AutoSaveWorld AutoPurgeThread");


		while (run) {
			// Prevent AutoPurge from never sleeping
			// If interval is 0, sleep for 10 seconds and skip purging
			if (config.purgeInterval == 0) {
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {}
				continue;
			}

			// Do our Sleep stuff!
			for (int i = 0; i < config.purgeInterval; i++) {
				if (!run) {break;}
				if (command) {break;}
				try {Thread.sleep(1000);} catch (InterruptedException e) {}
			}

			if (run && (config.purgeEnabled || command)) {
				if (plugin.checkCanDoOperation()) {
					plugin.setOperationInProgress(true);
					try {
						performPurge();
					} catch (Exception e) {
						e.printStackTrace();
					}
					plugin.setOperationInProgress(false);
				}
			}

		}

		plugin.debug("Graceful quit of AutoPurgeThread");

	}




	public void performPurge() {

		command = false;

		plugin.broadcast(configmsg.messagePurgeBroadcastPre, config.purgeBroadcast);

		plugin.debug("Purge started");


		plugin.debug("Purge finished");
		
		UniquePlayerIdentifierType type = UniquePlayerIdentifierDetector.getUniquePlayerIdentifierType();
		
		if (type == UniquePlayerIdentifierType.NAME) {
			new PurgeByNames(plugin, config).startPurge(); 
		}

		plugin.broadcast(configmsg.messagePurgeBroadcastPost, config.purgeBroadcast);

	}

}
