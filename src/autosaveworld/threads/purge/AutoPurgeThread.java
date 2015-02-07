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

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

import autosaveworld.config.AutoSaveWorldConfig;
import autosaveworld.config.AutoSaveWorldConfigMSG;
import autosaveworld.core.logging.MessageLogger;
import autosaveworld.threads.purge.plugins.DatfilePurge;
import autosaveworld.threads.purge.plugins.lwc.LWCPurge;
import autosaveworld.threads.purge.plugins.mywarp.MyWarpPurge;
import autosaveworld.threads.purge.plugins.permissions.PermissionsPurge;
import autosaveworld.threads.purge.plugins.residence.ResidencePurge;
import autosaveworld.threads.purge.plugins.wg.WGPurge;

public class AutoPurgeThread extends Thread {

	private AutoSaveWorldConfig config;
	private AutoSaveWorldConfigMSG configmsg;

	public AutoPurgeThread(AutoSaveWorldConfig config, AutoSaveWorldConfigMSG configmsg) {
		this.config = config;
		this.configmsg = configmsg;
	}

	public void stopThread() {
		run = false;
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
				if (!run || command) {
					break;
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}

			if (run && (config.purgeEnabled || command)) {
				command = false;
				try {
					performPurge();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		MessageLogger.debug("Graceful quit of AutoPurgeThread");

	}

	public void performPurge() {

		MessageLogger.broadcast(configmsg.messagePurgeBroadcastPre, config.purgeBroadcast);

		MessageLogger.debug("Purge started");

		MessageLogger.debug("Finiding active players");
		ActivePlayersList activelist = new ActivePlayersList(config);
		activelist.calculateActivePlayers(config.purgeAwayTime * 1000);
		MessageLogger.debug("Found " + activelist.getActivePlayersCount() + " active players");

		ArrayList<DataPurge> purges = new ArrayList<DataPurge>();
		PluginManager pm = Bukkit.getPluginManager();
		if ((pm.getPlugin("WorldGuard") != null) && config.purgeWG) {
			MessageLogger.debug("WG found, adding to purge list");
			purges.add(new WGPurge(config, activelist));
		}
		if ((pm.getPlugin("LWC") != null) && config.purgeLWC) {
			MessageLogger.debug("LWC found, adding to purge list");
			purges.add(new LWCPurge(config, activelist));
		}
		if ((pm.getPlugin("Residence") != null) && config.purgeResidence) {
			MessageLogger.debug("Residence found, adding to purge list");
			purges.add(new ResidencePurge(config, activelist));
		}
		if ((pm.getPlugin("MyWarp") != null) && config.purgeMyWarp) {
			MessageLogger.debug("MyWarp found, adding to purge list");
			purges.add(new MyWarpPurge(config, activelist));
		}
		if (config.purgePerms) {
			MessageLogger.debug("Permissions purge is enabled, adding to purge list");
			purges.add(new PermissionsPurge(config, activelist));
		}
		if (config.purgeDat) {
			MessageLogger.debug("Dat purge is enabled, adding to purge list");
			purges.add(new DatfilePurge(config, activelist));
		}

		for (DataPurge datapurge : purges) {
			try {
				datapurge.doPurge();
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}

		MessageLogger.debug("Purge finished");

		MessageLogger.broadcast(configmsg.messagePurgeBroadcastPost, config.purgeBroadcast);

	}

}
