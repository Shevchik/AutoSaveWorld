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

import org.bukkit.Bukkit;

import autosaveworld.config.AutoSaveConfig;
import autosaveworld.config.AutoSaveConfigMSG;
import autosaveworld.core.AutoSaveWorld;
import autosaveworld.threads.backup.ftp.FTPBackup;
import autosaveworld.threads.backup.localfs.LocalFSBackup;
import autosaveworld.threads.backup.script.ScriptBackup;

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
		if (plugin.backupInProgress) {
			plugin.warn("Multiple concurrent backups attempted! Backup interval is likely too short!");
			return;
		}
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


	private void performBackup()
	{
		command = false;
		
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
			if (config.backupsaveBefore)
			{
				int taskid = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
				{
						public void run()
						{
							plugin.saveThread.command = true;
							plugin.saveThread.performSave();
						}

				});
				while (Bukkit.getScheduler().isCurrentlyRunning(taskid) || Bukkit.getScheduler().isQueued(taskid))
				{
					try {Thread.sleep(100);} catch (Exception e) {e.printStackTrace();}
				}
			}
			// Lock
			plugin.backupInProgress = true;
			
			long timestart = System.currentTimeMillis();
			
			if (config.backupBroadcast){plugin.broadcast(configmsg.messageBackupBroadcastPre);}
		
			if (config.localfsbackupenabled)
			{
				plugin.debug("Starting LocalFS backup");
				new LocalFSBackup(plugin, config).performBackup();
				plugin.debug("LocalFS backup finished");
			}
			
			if (config.ftpbackupenabled)
			{
				plugin.debug("Starting FTP backup");
				new FTPBackup(plugin, config).performBackup();
				plugin.debug("FTP backup finished");
			}
			
			if (config.scriptbackupenabled)
			{
				plugin.debug("Starting Script Backup");
				new ScriptBackup(plugin, config).performBackup();
				plugin.debug("Script Backup Finished");
			}
		
			plugin.debug("Full backup time: "+(System.currentTimeMillis()-timestart)+" milliseconds");
			if (config.backupBroadcast){plugin.broadcast(configmsg.messageBackupBroadcastPost);}
			plugin.LastBackup =new java.text.SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(System.currentTimeMillis());

		} 
		finally 
		{
			// Release
			plugin.backupInProgress = false;
		}
	}
	
}
	



