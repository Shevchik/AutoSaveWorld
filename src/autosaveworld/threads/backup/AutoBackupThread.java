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

import autosaveworld.config.AutoSaveConfig;
import autosaveworld.config.AutoSaveConfigMSG;
import autosaveworld.core.AutoSaveWorld;

public class AutoBackupThread extends Thread {

	private AutoSaveWorld plugin = null;
	private AutoSaveConfig config;
	private AutoSaveConfigMSG configmsg;
	public AutoBackupThread(AutoSaveWorld plugin, AutoSaveConfig config, AutoSaveConfigMSG configmsg) {
		this.plugin = plugin;
		this.config = config;
		this.configmsg = configmsg;
	}
	

	public void stopThread() {
		this.run = false;
	}
	
	public void startbackup() {
		command = true;
	}
    
	
	// The code to run...weee
	private volatile boolean run = true;
    private boolean command = false;
	public void run() {

		plugin.debug("AutoBackupThread Started");
		Thread.currentThread().setName("AutoSaveWorld AutoBackupThread");
		
		while (run) {
			// Prevent AutoBackup from never sleeping
			// If interval is 0, sleep for 10 seconds and skip backup
			if(config.backupInterval == 0) {
				try {Thread.sleep(10000);} catch(InterruptedException e) {}
				continue;
			}
			
			// Do our Sleep stuff!
			for (int i = 0; i < config.backupInterval; i++) {
				if (!run) {break;}
				if (command) {break;}
				try {Thread.sleep(1000);} catch (InterruptedException e) {}
			}
			
			if (run&&(config.backupEnabled||command)) {performBackup();}
			
		}
		
		plugin.debug("Graceful quit of AutoBackupThread");

	}

	public long datesec;
	private void performBackup()
	{
		command = false;
		
		if (plugin.backupInProgress) {
			plugin.warn("Multiple concurrent backups attempted! Backup interval is likely too short!");
			return;
		}
		if (plugin.purgeInProgress) {
			plugin.warn("AutoPurge is in progress. Backup cancelled.");
			return;
		}
		if (plugin.saveInProgress) {
			plugin.warn("AutoSave is in progress. Backup cancelled.");	
			return;
		}
		if (plugin.worldregenInProcess) {
			plugin.warn("WorldRegen is in progress. Backup cancelled.");
			return;
		}
		
		try 
		{
			// Lock
			plugin.backupInProgress = true;
			if (config.backupBroadcast){plugin.broadcast(configmsg.messageBackupBroadcastPre);}
		
			datesec = System.currentTimeMillis();
		
			if (config.localfsbackupenabled)
			{
				new FlatBackup(plugin, config).performBackup();
			}
		
			plugin.debug("Full backup time: "+(System.currentTimeMillis()-datesec)+" milliseconds");
			if (config.backupBroadcast){plugin.broadcast(configmsg.messageBackupBroadcastPost);}
			plugin.LastBackup =new java.text.SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(java.util.Calendar.getInstance().getTime());

		} 
		finally 
		{
			// Release
			plugin.backupInProgress = false;
		}
	}
	
}
	



