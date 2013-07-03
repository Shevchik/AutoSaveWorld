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

import java.util.logging.Logger;

import org.bukkit.Bukkit;

import autosaveworld.config.AutoSaveConfig;
import autosaveworld.config.AutoSaveConfigMSG;
import autosaveworld.core.AutoSaveWorld;

public class AutoPurgeThread extends Thread {

	protected final Logger log = Bukkit.getLogger();
	private AutoSaveWorld plugin = null;
	private AutoSaveConfig config;
	private AutoSaveConfigMSG configmsg;
	private volatile boolean run = true;
	private boolean command = false;
	
	public AutoPurgeThread(AutoSaveWorld plugin, AutoSaveConfig config,
			AutoSaveConfigMSG configmsg) {
		this.plugin = plugin;
		this.config = config;
		this.configmsg = configmsg;
	}

	// Allows for the thread to naturally exit if value is false
	public void stopThread() {
		this.run = false;
	}

	private int i;

	public void startpurge() {
		command = true;
		i = config.purgeInterval;
	}

	
	// The code to run...weee
	public void run() {

		log.info(String.format("[%s] AutoPurgeThread Started: Interval is %d seconds",
						plugin.getDescription().getName(), config.purgeInterval
					)
				);
		Thread.currentThread().setName("AutoSaveWorld AutoPurgeThread");

		
		while (run) {
			// Prevent AutoPurge from never sleeping
			// If interval is 0, sleep for 10 seconds and skip saving
			if (config.purgeInterval == 0) {
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {}
				continue;
			}

			// Do our Sleep stuff!
			for (i = 0; i < config.purgeInterval; i++) {
				try {Thread.sleep(1000);} catch (InterruptedException e) {log.info("Could not sleep!");}
			}

			if (config.purgeEnabled || command) {performPurge();}

		}
		
		//message before disabling thread
		if (config.varDebug) {log.info("[AutoSaveWorld] Graceful quit of AutoPurgeThread");}
		
	}




	public void performPurge() {
		//do not purge if one of this is running or this may end bad
		if (plugin.purgeInProgress) {
			plugin.warn("Multiple concurrent purges attempted! Purge interval is likely too short!");
			return;
		} 
		if (plugin.backupInProgress) {
			plugin.warn("AutoBackup is in progress. Purge cancelled.");
			return;
		}
			
			command = false;
			
			plugin.purgeInProgress = true;
			if (config.purgeBroadcast) {
				plugin.broadcast(configmsg.messagePurgePre);
			}
			
			long awaytime = config.purgeAwayTime * 1000;
			
			plugin.debug("Purge started");
			
			if ((plugin.getServer().getPluginManager().getPlugin("WorldGuard") != null)
					&& config.wg) {
				plugin.debug("WG found, purging");
				try {
					new WGpurge(plugin, awaytime, config.wgregenrg, config.wgnoregenoverlap);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			if ((plugin.getServer().getPluginManager().getPlugin("LWC") != null)
					&& config.lwc) {
				plugin.debug("LWC found, purging");
				try {
					new LWCpurge(plugin, awaytime, config.lwcdelprotectedblocks);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			if ((Bukkit.getPluginManager().getPlugin("Multiverse-Inventories") !=null) 
					&& config.mvinv ) {
				plugin.debug("Multiverse-Inventories found, purging");
				try {
					new MVInvpurge(plugin, awaytime);
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			if ((Bukkit.getPluginManager().getPlugin("PlotMe") !=null) 
					&& config.pm) {
				plugin.debug("PlotMe found, purging");
				try {
					new PlotMepurge(plugin, awaytime, config.pmregen);
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			plugin.debug("Purging player .dat files");
			if (config.dat) {
				try {
					new Datfilepurge(plugin, awaytime);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			

			
			plugin.debug("Purge finished");
			

			if (config.purgeBroadcast) {
				plugin.broadcast(configmsg.messagePurgePost);
			}
			
			plugin.purgeInProgress = false;
			
	}


}
