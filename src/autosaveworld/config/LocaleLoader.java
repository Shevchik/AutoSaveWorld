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

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class LocaleLoader {

	private AutoSaveConfigMSG configmsg;
	public LocaleLoader(AutoSaveConfigMSG configmsg)
	{
		this.configmsg = configmsg;
	}
	
	//load messages from config
	public void loadLocaleMessegaes(File localeFile)
	{
		FileConfiguration cfg = YamlConfiguration.loadConfiguration(localeFile);
		configmsg.messageSaveBroadcastPre = cfg.getString("broadcast.pre", configmsg.messageSaveBroadcastPre);
		configmsg.messageSaveBroadcastPost = cfg.getString("broadcast.post", configmsg.messageSaveBroadcastPost);
		configmsg.messageBackupBroadcastPre = cfg.getString("broadcastbackup.pre", configmsg.messageBackupBroadcastPre);
		configmsg.messageBackupBroadcastPost = cfg.getString("broadcastbackup.post", configmsg.messageBackupBroadcastPost);
		configmsg.messagePurgeBroadcastPre = cfg.getString("broadcastpurge.pre", configmsg.messagePurgeBroadcastPre);
		configmsg.messagePurgeBroadcastPost = cfg.getString("broadcastpurge.post", configmsg.messagePurgeBroadcastPost);
		configmsg.messageInsufficientPermissions = cfg.getString("insufficentpermissions", configmsg.messageInsufficientPermissions);
		configmsg.messageAutoRestart = cfg.getString("autorestart.restarting", configmsg.messageAutoRestart);
		configmsg.messageAutoRestartCountdown = cfg.getString("autorestart.countdown", configmsg.messageAutoRestartCountdown);
		configmsg.messageWorldRegenKick = cfg.getString("worldregen.kickmessage", configmsg.messageWorldRegenKick);
	}

}
