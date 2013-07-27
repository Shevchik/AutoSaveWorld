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

package autosaveworld.threads.backup;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import autosaveworld.config.AutoSaveConfig;
import autosaveworld.config.AutoSaveConfigMSG;
import autosaveworld.core.AutoSaveWorld;
import autosaveworld.core.Generic;

public class AutoBackupThread extends Thread {

	protected final Logger log = Bukkit.getLogger();
	private volatile boolean run = true;
	private AutoSaveWorld plugin = null;
	private AutoSaveConfig config;
	private AutoSaveConfigMSG configmsg;
    private int i;
    private boolean command = false;
    public long datesec;

	
	// Constructor to define number of seconds to sleep
	public AutoBackupThread(AutoSaveWorld plugin, AutoSaveConfig config, AutoSaveConfigMSG configmsg) {
		this.plugin = plugin;
		this.config = config;
		this.configmsg = configmsg;
	}
	

    
	// Allows for the thread to naturally exit if value is false
	public void stopThread() {
		this.run = false;
	}
	
	public void startbackup()
	{
	command = true;
	i = config.backupInterval;
	}
    
	
	// The code to run...weee
	public void run() {

		log.info(String.format("[%s] AutoBackupThread Started: Interval is %d seconds",
						plugin.getDescription().getName(), config.backupInterval
					)
				);
		Thread.currentThread().setName("AutoSaveWorld AutoBackupThread");
		
		while (run) {
			// Prevent AutoBackup from never sleeping
			// If interval is 0, sleep for 10 seconds and skip backup
			if(config.backupInterval == 0) {
				try {
					Thread.sleep(10000);
				} catch(InterruptedException e) {}
				continue;
			}
			
			// Do our Sleep stuff!
			for (i = 0; i < config.backupInterval; i++) {
				try {
										
					boolean warn = config.backupwarn;
					for (int w : config.backupWarnTimes) {
						if (w != 0 && w + i == config.backupInterval) {
							
						} else {warn = false;}
					}

					if (warn) {
						// Perform warning
						if (config.backupEnabled) {
							plugin.getServer().broadcastMessage(Generic.parseColor(configmsg.messageBackupWarning));
							log.info(String.format("[%s] %s", plugin.getDescription().getName(), configmsg.messageBackupWarning));
						}
					}
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					log.info("Could not sleep!");
				}
			}
				if (config.backupEnabled||command) {performBackup();}
		}
		
		if (config.varDebug) {
			log.info("[AutoSaveWorld] Graceful quit of AutoBackupThread");
		}
	}
	
	
	private void performBackup()
	{
		if (plugin.backupInProgress) {
			plugin.warn("Multiple concurrent backups attempted! Backup interval is likely too short!");
			return;
		} else if (plugin.purgeInProgress) {
			plugin.warn("AutoPurge is in progress. Backup cancelled.");
			return;
		} else if (plugin.saveInProgress) {
			plugin.warn("AutoSave is in progress. Backup cancelled.");	
			return;
		} else if (plugin.worldregenInProcess)
		{
			plugin.warn("WorldRegen is in progress. Backup cancelled.");
			return;
		} else {
		
		try {
		// Lock
		plugin.saveInProgress = true;
		plugin.backupInProgress = true;
		if (config.backupBroadcast){plugin.broadcast(configmsg.messageBroadcastBackupPre);}
		
		
		if (config.flatbackupenabled)
		{
			new FlatBackup(plugin, config).performBackup();
		}
		
		
		plugin.debug("Full backup time: "+(System.currentTimeMillis()-datesec)+" milliseconds");
		if (config.backupBroadcast){plugin.broadcast(configmsg.messageBroadcastBackupPost);}
		plugin.LastBackup =new java.text.SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(java.util.Calendar.getInstance().getTime());
		// Release
		} finally {
		command = false;
		plugin.saveInProgress = false;
		plugin.backupInProgress = false;
		}
		}
	}
	
	

	
    
	


	
	


	



	


	
	
}


