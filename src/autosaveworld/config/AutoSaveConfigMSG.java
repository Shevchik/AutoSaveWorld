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

import autosaveworld.core.AutoSaveWorld;

public class AutoSaveConfigMSG {
	private FileConfiguration configmsg;
	private AutoSaveConfig config;
	private AutoSaveWorld plugin;
	public AutoSaveConfigMSG(AutoSaveWorld plugin, AutoSaveConfig config) {
		this.plugin = plugin;
		this.config = config;
	}
	// Messages
	public String messageSaveBroadcastPre = "&9AutoSaving";
	public String messageSaveBroadcastPost = "&9AutoSave Complete";
	public String messageBackupBroadcastPre = "&9AutoBackuping";
	public String messageBackupBroadcastPost = "&9AutoBackup Complete";
	public String messagePurgeBroadcastPre = "&9AutoPurging";
	public String messagePurgeBroadcastPost = "&9AutoPurge Complete";
	public String messageAutoRestart = "&9Server is restarting";
	public String messageAutoRestartCountdown = "&9Server will restart in {SECONDS} seconds";
	public String messageWorldRegenKick = "&9Server is regenerating map, please come back later";
	public String messageInsufficientPermissions = "&cYou do not have access to that command.";
	
	public void loadmsg() {
		if (!config.switchtolangfile) {
			configmsg = YamlConfiguration.loadConfiguration(new File(plugin.constants.getConfigMSGPath()));
			loadMessages();
			saveMessages();
		} else
		{
			configmsg = YamlConfiguration.loadConfiguration(new File(plugin.constants.getConfigMSGWithSuffix(config.langfilesuffix)));
			loadMessages();
		}
	}
	
	
	private void loadMessages()
	{
		messageSaveBroadcastPre =configmsg.getString("broadcast.pre", messageSaveBroadcastPre);
		messageSaveBroadcastPost =configmsg.getString("broadcast.post", messageSaveBroadcastPost);
		messageBackupBroadcastPre =configmsg.getString("broadcastbackup.pre", messageBackupBroadcastPre);
		messageBackupBroadcastPost =configmsg.getString("broadcastbackup.post", messageBackupBroadcastPost);
		messagePurgeBroadcastPre =configmsg.getString("broadcastpurge.pre", messagePurgeBroadcastPre);
		messagePurgeBroadcastPost =configmsg.getString("broadcastpurge.post", messagePurgeBroadcastPost);
		messageInsufficientPermissions =configmsg.getString("insufficentpermissions", messageInsufficientPermissions);
		messageAutoRestart = configmsg.getString("autorestart.restarting",messageAutoRestart);
		messageAutoRestartCountdown = configmsg.getString("autorestart.countdown",messageAutoRestartCountdown);
		messageWorldRegenKick = configmsg.getString("worldregen.kickmessage", messageWorldRegenKick);
	}
	
	private void saveMessages()
	{
		configmsg = new YamlConfiguration();
		configmsg.set("broadcast.pre", messageSaveBroadcastPre);
		configmsg.set("broadcast.post", messageSaveBroadcastPost);
		configmsg.set("broadcastbackup.pre", messageBackupBroadcastPre);
		configmsg.set("broadcastbackup.post", messageBackupBroadcastPost);
		configmsg.set("broadcastpurge.pre", messagePurgeBroadcastPre);
		configmsg.set("broadcastpurge.post", messagePurgeBroadcastPost);
		configmsg.set("autorestart.restarting",messageAutoRestart);
		configmsg.set("autorestart.countdown",messageAutoRestartCountdown);
		configmsg.set("worldregen.kickmessage", messageWorldRegenKick);
		configmsg.set("insufficentpermissions", messageInsufficientPermissions);
		try {
			configmsg.save(new File("plugins/AutoSaveWorld/configmsg.yml"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	
}
