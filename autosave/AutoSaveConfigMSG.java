/**
 * 
 * Copyright 2012 Shevchik
 * 
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
package autosave;

import java.io.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class AutoSaveConfigMSG {
	private FileConfiguration configmsg;
	private AutoSaveConfig config;
	public AutoSaveConfigMSG(AutoSaveConfig config) {
		this.config = config;
	}
	// Messages
	protected String messageBroadcastPre = "&9AutoSaving";
	protected String messageBroadcastPost = "&9AutoSave Complete";
	protected String messageInsufficientPermissions = "&cYou do not have access to that command.";
	protected String messageWarning = "&9Warning, AutoSave will commence soon.";
	protected String messageBroadcastBackupPre = "&9AutoBackuping";
	protected String messageBroadcastBackupPost = "&9AutoBackup Complete";
	protected String messageBackupWarning = "&9Warning, AutoBackup will commence soon";
	protected String messagePurgePre = "&9AutoPurging";
	protected String messagePurgePost = "&9AutoPurge Complete";
	protected String messageAutoRestart = "&9Server is restarting";
	protected String messageAutoRestartCountdown = "&9Server will restart in {SECONDS} seconds";
	
	public void loadmsg() {
		if (!config.switchtolangfile) {
		configmsg = YamlConfiguration.loadConfiguration(new File("plugins/AutoSaveWorld/configmsg.yml"));
		messageBroadcastPre =configmsg.getString("broadcast.pre", messageBroadcastPre);
		messageBroadcastPost =configmsg.getString("broadcast.post", messageBroadcastPost);
		messageBroadcastBackupPre =configmsg.getString("broadcastbackup.pre", messageBroadcastBackupPre);
		messageBroadcastBackupPost =configmsg.getString("broadcastbackup.post", messageBroadcastBackupPost);
		messagePurgePre =configmsg.getString("broadcastpurge.pre", messagePurgePre);
		messagePurgePost =configmsg.getString("broadcastpurge.post", messagePurgePost);
		messageWarning =configmsg.getString("warning.save", messageWarning);
		messageBackupWarning =configmsg.getString("warning.backup", messageBackupWarning);
		messageInsufficientPermissions =configmsg.getString("insufficentpermissions", messageInsufficientPermissions);
		messageAutoRestart = configmsg.getString("autorestart.restarting",messageAutoRestart);
		messageAutoRestartCountdown = configmsg.getString("autorestart.countdown",messageAutoRestartCountdown);
		configmsg = new YamlConfiguration();
		configmsg.set("broadcast.pre", messageBroadcastPre);
		configmsg.set("broadcast.post", messageBroadcastPost);
		configmsg.set("broadcastbackup.pre", messageBroadcastBackupPre);
		configmsg.set("broadcastbackup.post", messageBroadcastBackupPost);
		configmsg.set("broadcastpurge.pre", messagePurgePre);
		configmsg.set("broadcastpurge.post", messagePurgePost);
		configmsg.set("warning.save", messageWarning);
		configmsg.set("warning.backup", messageBackupWarning);
		configmsg.set("insufficentpermissions", messageInsufficientPermissions);
		configmsg.set("autorestart.restarting",messageAutoRestart);
		configmsg.set("autorestart.countdown",messageAutoRestartCountdown);
		try {
			configmsg.save(new File("plugins/AutoSaveWorld/configmsg.yml"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		} else
		{
			configmsg = YamlConfiguration.loadConfiguration(new File("plugins/AutoSaveWorld/configmsg_"+config.langfilesuffix+".yml"));
			messageBroadcastPre =configmsg.getString("broadcast.pre", messageBroadcastPre);
			messageBroadcastPost =configmsg.getString("broadcast.post", messageBroadcastPost);
			messageBroadcastBackupPre =configmsg.getString("broadcastbackup.pre", messageBroadcastBackupPre);
			messageBroadcastBackupPost =configmsg.getString("broadcastbackup.post", messageBroadcastBackupPost);
			messagePurgePre =configmsg.getString("broadcastpurge.pre", messagePurgePre);
			messagePurgePost =configmsg.getString("broadcastpurge.post", messagePurgePost);
			messageWarning =configmsg.getString("warning.save", messageWarning); 
			messageBackupWarning =configmsg.getString("warning.backup", messageBackupWarning);
			messageInsufficientPermissions =configmsg.getString("insufficentpermissions", messageInsufficientPermissions);
			messageAutoRestart = configmsg.getString("autorestart.restarting",messageAutoRestart);
			messageAutoRestartCountdown = configmsg.getString("autorestart.countdown",messageAutoRestartCountdown);

		}
		

		
	}
	
	
	
	
}
