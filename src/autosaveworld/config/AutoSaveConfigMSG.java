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

package autosaveworld.config;

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
	public String messageBroadcastPre = "&9AutoSaving";
	public String messageBroadcastPost = "&9AutoSave Complete";
	public String messageInsufficientPermissions = "&cYou do not have access to that command.";
	public String messageWarning = "&9Warning, AutoSave will commence soon.";
	public String messageBroadcastBackupPre = "&9AutoBackuping";
	public String messageBroadcastBackupPost = "&9AutoBackup Complete";
	public String messageBackupWarning = "&9Warning, AutoBackup will commence soon";
	public String messagePurgePre = "&9AutoPurging";
	public String messagePurgePost = "&9AutoPurge Complete";
	public String messageAutoRestart = "&9Server is restarting";
	public String messageAutoRestartCountdown = "&9Server will restart in {SECONDS} seconds";
	public String messageWorldRegenKick = "&9Server is regenerating map, please come back later";
	
	public void loadmsg() {
		if (!config.switchtolangfile) {
			configmsg = YamlConfiguration.loadConfiguration(new File("plugins/AutoSaveWorld/configmsg.yml"));
			loadMessages();
			saveMessages();
		} else
		{
			configmsg = YamlConfiguration.loadConfiguration(new File("plugins/AutoSaveWorld/configmsg_"+config.langfilesuffix+".yml"));
			loadMessages();
		}
		

		
	}
	
	
	private void loadMessages()
	{
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
		messageWorldRegenKick = configmsg.getString("worldregen.kickmessage", messageWorldRegenKick);
	}
	
	private void saveMessages()
	{
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
		configmsg.set("worldregen.kickmessage", messageWorldRegenKick);
		try {
			configmsg.save(new File("plugins/AutoSaveWorld/configmsg.yml"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	
}
