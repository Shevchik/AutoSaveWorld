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

import org.bukkit.plugin.PluginManager;

import autosaveworld.config.AutoSaveWorldConfig;
import autosaveworld.config.AutoSaveConfigMSG;
import autosaveworld.core.AutoSaveWorld;
import autosaveworld.threads.purge.plugins.DatfilePurge;
import autosaveworld.threads.purge.plugins.LWCPurge;
import autosaveworld.threads.purge.plugins.MVInvPurge;
import autosaveworld.threads.purge.plugins.MyWarpPurge;
import autosaveworld.threads.purge.plugins.PlotMePurge;
import autosaveworld.threads.purge.plugins.ResidencePurge;
import autosaveworld.threads.purge.plugins.VaultPurge;
import autosaveworld.threads.purge.plugins.WGPurge;

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

		long awaytime = config.purgeAwayTime * 1000;

		plugin.debug("Purge started");

		plugin.debug("Gathering active players list");
		ActivePlayersList aplist = new ActivePlayersList(plugin, config);
		aplist.gatherActivePlayersList(awaytime);
		plugin.debug("Found "+aplist.getActivePlayersCount()+" active players");

		PluginManager pm = plugin.getServer().getPluginManager();

		if ((pm.getPlugin("WorldGuard") != null) && config.purgewg) {
			plugin.debug("WG found, purging");
			try {
				new WGPurge(plugin).doWGPurgeTask(aplist, config.purgewgregenrg, config.purgewgnoregenoverlap);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if ((pm.getPlugin("LWC") != null) && config.purgelwc) {
			plugin.debug("LWC found, purging");
			try {
				new LWCPurge(plugin).doLWCPurgeTask(aplist, config.purgelwcdelprotectedblocks);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if ((pm.getPlugin("Multiverse-Inventories") !=null) && config.purgemvinv) {
			plugin.debug("Multiverse-Inventories found, purging");
			try {
				new MVInvPurge(plugin).doMVInvPurgeTask(aplist);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if ((pm.getPlugin("PlotMe") !=null) && config.purgepm) {
			plugin.debug("PlotMe found, purging");
			try {
				new PlotMePurge(plugin).doPlotMePurgeTask(aplist, config.purgepmregen);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if ((pm.getPlugin("Residence") !=null) && config.purgeresidence) {
			plugin.debug("Residence found, purging");
			try {
				new ResidencePurge(plugin).doResidencePurgeTask(aplist, config.purgeresregenarena);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (pm.getPlugin("Vault") != null) {
			VaultPurge vp = new VaultPurge(plugin);
			if (config.purgeeconomy) {
				plugin.debug("Vault found, purging economy");
				vp.doEconomyPurgeTask(aplist);
			}
			if (config.purgeperms) {
				plugin.debug("Vault found, purging permissions");
				vp.doPermissionsPurgeTask(aplist);
			}
		}

		if (pm.getPlugin("MyWarp") != null && config.purgemywarp) {
			plugin.debug("MyWarp found, purging");
			try {
				new MyWarpPurge(plugin).doMyWarpPurgeTask(aplist);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		plugin.debug("Purging player .dat files");
		if (config.purgedat) {
			try {
				new DatfilePurge(plugin).doDelPlayerDatFileTask(aplist);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		plugin.debug("Purge finished");


		plugin.broadcast(configmsg.messagePurgeBroadcastPost, config.purgeBroadcast);

	}

}
