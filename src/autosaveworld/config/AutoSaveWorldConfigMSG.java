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

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import autosaveworld.core.GlobalConstants;

public class AutoSaveWorldConfigMSG {

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
		FileConfiguration configfile = YamlConfiguration.loadConfiguration(new File(GlobalConstants.getConfigMSGPath()));
		messageSaveBroadcastPre = configfile.getString("broadcast.pre", messageSaveBroadcastPre);
		messageSaveBroadcastPost = configfile.getString("broadcast.post", messageSaveBroadcastPost);
		messageBackupBroadcastPre = configfile.getString("broadcastbackup.pre", messageBackupBroadcastPre);
		messageBackupBroadcastPost = configfile.getString("broadcastbackup.post", messageBackupBroadcastPost);
		messagePurgeBroadcastPre = configfile.getString("broadcastpurge.pre", messagePurgeBroadcastPre);
		messagePurgeBroadcastPost = configfile.getString("broadcastpurge.post", messagePurgeBroadcastPost);
		messageInsufficientPermissions = configfile.getString("insufficentpermissions", messageInsufficientPermissions);
		messageAutoRestart = configfile.getString("autorestart.restarting", messageAutoRestart);
		messageAutoRestartCountdown = configfile.getString("autorestart.countdown", messageAutoRestartCountdown);
		messageWorldRegenKick = configfile.getString("worldregen.kickmessage", messageWorldRegenKick);
		savemsg();
	}

	private void savemsg() {
		FileConfiguration configfile = new YamlConfiguration();
		configfile.set("broadcast.pre", messageSaveBroadcastPre);
		configfile.set("broadcast.post", messageSaveBroadcastPost);
		configfile.set("broadcastbackup.pre", messageBackupBroadcastPre);
		configfile.set("broadcastbackup.post", messageBackupBroadcastPost);
		configfile.set("broadcastpurge.pre", messagePurgeBroadcastPre);
		configfile.set("broadcastpurge.post", messagePurgeBroadcastPost);
		configfile.set("insufficentpermissions", messageInsufficientPermissions);
		configfile.set("autorestart.restarting", messageAutoRestart);
		configfile.set("autorestart.countdown", messageAutoRestartCountdown);
		configfile.set("worldregen.kickmessage", messageWorldRegenKick);
		try {
			configfile.save(new File(GlobalConstants.getConfigMSGPath()));
		} catch (IOException e) {
		}
	}

}
