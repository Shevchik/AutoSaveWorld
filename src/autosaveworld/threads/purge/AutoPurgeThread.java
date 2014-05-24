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
import autosaveworld.config.AutoSaveWorldConfigMSG;
import autosaveworld.core.AutoSaveWorld;
import autosaveworld.core.logging.MessageLogger;
import autosaveworld.threads.purge.UniquePlayerIdentifierDetector.UniquePlayerIdentifierType;
import autosaveworld.threads.purge.bynames.PurgeByNames;
import autosaveworld.threads.purge.byuuids.PurgeByUUIDs;

public class AutoPurgeThread extends Thread {

	private AutoSaveWorld plugin = null;
	private AutoSaveWorldConfig config;
	private AutoSaveWorldConfigMSG configmsg;
	public AutoPurgeThread(AutoSaveWorld plugin, AutoSaveWorldConfig config, AutoSaveWorldConfigMSG configmsg) {
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

		MessageLogger.debug("AutoPurgeThread Started");
		Thread.currentThread().setName("AutoSaveWorld AutoPurgeThread");


		while (run) {
			// Do our Sleep stuff!
			for (int i = 0; i < config.purgeInterval; i++) {
				if (!run) {break;}
				if (command) {break;}
				try {Thread.sleep(1000);} catch (InterruptedException e) {}
			}

			if (run && (config.purgeEnabled || command)) {
				plugin.lock.lock();
				try {
					performPurge();
				} catch (Exception e) {
					e.printStackTrace();
				}
				plugin.lock.unlock();
			}
		}

		MessageLogger.debug("Graceful quit of AutoPurgeThread");

	}




	public void performPurge() {

		command = false;

		MessageLogger.broadcast(configmsg.messagePurgeBroadcastPre, config.purgeBroadcast);

		MessageLogger.debug("Purge started");

		MessageLogger.debug("Getting player unique identifier type");
		UniquePlayerIdentifierType type = UniquePlayerIdentifierDetector.getUniquePlayerIdentifierType();

		MessageLogger.debug("Player unique identifier type is "+type.toString());
		if (type == UniquePlayerIdentifierType.NAME) {
			new PurgeByNames(plugin, config).startPurge();
		} else if (type == UniquePlayerIdentifierType.UUID) {
			new PurgeByUUIDs(plugin, config).startPurge();
		}

		MessageLogger.debug("Purge finished");

		MessageLogger.broadcast(configmsg.messagePurgeBroadcastPost, config.purgeBroadcast);

	}

}
