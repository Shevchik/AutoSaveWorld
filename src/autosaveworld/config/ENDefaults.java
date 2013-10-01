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

package autosaveworld.config;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class ENDefaults {

	private AutoSaveConfigMSG configmsg;
	public ENDefaults(AutoSaveConfigMSG configmsg) {
		this.configmsg = configmsg;
	}
	
	private String messageSaveBroadcastPre = "&9AutoSaving";
	private String messageSaveBroadcastPost = "&9AutoSave Complete";
	private String messageBackupBroadcastPre = "&9AutoBackuping";
	private String messageBackupBroadcastPost = "&9AutoBackup Complete";
	private String messagePurgeBroadcastPre = "&9AutoPurging";
	private String messagePurgeBroadcastPost = "&9AutoPurge Complete";
	private String messageAutoRestart = "&9Server is restarting";
	private String messageAutoRestartCountdown = "&9Server will restart in {SECONDS} seconds";
	private String messageWorldRegenKick = "&9Server is regenerating map, please come back later";
	private String messageInsufficientPermissions = "&cYou do not have access to that command.";
	
	public void loadMessagesEN()
	{
		configmsg.messageSaveBroadcastPre = this.messageSaveBroadcastPre;
		configmsg.messageSaveBroadcastPost = this.messageSaveBroadcastPost;
		configmsg.messageBackupBroadcastPre = this.messageBackupBroadcastPre;
		configmsg.messageBackupBroadcastPost = this.messageBackupBroadcastPost;
		configmsg.messagePurgeBroadcastPre = this.messagePurgeBroadcastPre;
		configmsg.messagePurgeBroadcastPost = this.messagePurgeBroadcastPost;
		configmsg.messageAutoRestart = this.messageAutoRestart;
		configmsg.messageAutoRestartCountdown = this.messageAutoRestartCountdown;
		configmsg.messageWorldRegenKick = this.messageWorldRegenKick;
		configmsg.messageInsufficientPermissions = this.messageInsufficientPermissions;
	}
	
	
	public void saveMessagesEN(File configfile)
	{
		FileConfiguration cfg = new YamlConfiguration();
		cfg.set("broadcast.pre", messageSaveBroadcastPre);
		cfg.set("broadcast.post", messageSaveBroadcastPost);
		cfg.set("broadcastbackup.pre", messageBackupBroadcastPre);
		cfg.set("broadcastbackup.post", messageBackupBroadcastPost);
		cfg.set("broadcastpurge.pre", messagePurgeBroadcastPre);
		cfg.set("broadcastpurge.post", messagePurgeBroadcastPost);
		cfg.set("autorestart.restarting",messageAutoRestart);
		cfg.set("autorestart.countdown",messageAutoRestartCountdown);
		cfg.set("worldregen.kickmessage", messageWorldRegenKick);
		cfg.set("insufficentpermissions", messageInsufficientPermissions);
		try {
			cfg.save(configfile);
		} catch (IOException e) {}
	}
}
