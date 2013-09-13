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

import org.bukkit.Bukkit;

import autosaveworld.config.AutoSaveConfig;
import autosaveworld.config.AutoSaveConfigMSG;
import autosaveworld.core.AutoSaveWorld;

public class AutoPurgeThread extends Thread {

	private AutoSaveWorld plugin = null;
	private AutoSaveConfig config;
	private AutoSaveConfigMSG configmsg;
	public AutoPurgeThread(AutoSaveWorld plugin, AutoSaveConfig config,
			AutoSaveConfigMSG configmsg) {
		this.plugin = plugin;
		this.config = config;
		this.configmsg = configmsg;
	}


	public void stopThread() {
		this.run = false;
	}

	public void startpurge() {
		if (plugin.purgeInProgress) {
			plugin.warn("Multiple concurrent purges attempted! Purge interval is likely too short!");
			return;
		}
		command = true;
	}

	// The code to run...weee
	private volatile boolean run = true;
	private boolean command = false;
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

			if (run&&(config.purgeEnabled || command)) {performPurge();}

		}
		
		plugin.debug("Graceful quit of AutoPurgeThread");
		
	}




	public void performPurge() {
		
		command = false;

		if (plugin.backupInProgress) {
			plugin.warn("AutoBackup is in progress. Purge cancelled.");
			return;
		}
		if (plugin.worldregenInProcess) {
			plugin.warn("WorldRegen is in progress. Purge cancelled.");
			return;
		}
			
			plugin.purgeInProgress = true;
			
			if (config.purgeBroadcast) {
				plugin.broadcast(configmsg.messagePurgeBroadcastPre);
			}
			
			long awaytime = config.purgeAwayTime * 1000;
			
			plugin.debug("Purge started");
			
			plugin.debug("Gathering active players list");
			PlayerActiveCheck pacheck = new PlayerActiveCheck(awaytime);
			
			if ((plugin.getServer().getPluginManager().getPlugin("WorldGuard") != null)
					&& config.purgewg) {
				plugin.debug("WG found, purging");
				try {
					new WGpurge(plugin).doWGPurgeTask(pacheck, config.purgewgregenrg, config.purgewgnoregenoverlap);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			if ((plugin.getServer().getPluginManager().getPlugin("LWC") != null)
					&& config.purgelwc) {
				plugin.debug("LWC found, purging");
				try {
					new LWCpurge(plugin).doLWCPurgeTask(awaytime, config.purgelwcdelprotectedblocks);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			if ((Bukkit.getPluginManager().getPlugin("Multiverse-Inventories") !=null) 
					&& config.purgemvinv ) {
				plugin.debug("Multiverse-Inventories found, purging");
				try {
					new MVInvpurge(plugin).doMVInvPurgeTask(awaytime);
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			if ((Bukkit.getPluginManager().getPlugin("PlotMe") !=null) 
					&& config.purgepm) {
				plugin.debug("PlotMe found, purging");
				try {
					new PlotMepurge(plugin).doPlotMePurgeTask(awaytime, config.purgepmregen);
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			if ((Bukkit.getPluginManager().getPlugin("Residence") !=null) 
					&& config.purgeresidence) {
				plugin.debug("Residence found, purging");
				try {
					new Residencepurge(plugin).doResidencePurgeTask(awaytime, config.purgeresregenarena);
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			plugin.debug("Purging player .dat files");
			if (config.purgedat) {
				try {
					new Datfilepurge(plugin).doDelPlayerDatFileTask(pacheck);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			

			
			plugin.debug("Purge finished");
			

			if (config.purgeBroadcast) {
				plugin.broadcast(configmsg.messagePurgeBroadcastPost);
			}
			
			plugin.purgeInProgress = false;
			
	}


}
