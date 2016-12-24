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

import autosaveworld.config.loader.Config;
import autosaveworld.config.loader.ConfigOption;
import autosaveworld.core.GlobalConstants;

public class AutoSaveWorldConfigMSG implements Config {

	@ConfigOption(path = "broadcast.pre")
	public String messageSaveBroadcastPre = "&9AutoSaving";
	@ConfigOption(path = "broadcast.post")
	public String messageSaveBroadcastPost = "&9AutoSave Complete";
	@ConfigOption(path = "broadcastbackup.pre")
	public String messageBackupBroadcastPre = "&9AutoBackuping";
	@ConfigOption(path = "broadcastbackup.post")
	public String messageBackupBroadcastPost = "&9AutoBackup Complete";
	@ConfigOption(path = "broadcastpurge.pre")
	public String messagePurgeBroadcastPre = "&9AutoPurging";
	@ConfigOption(path = "broadcastpurge.post")
	public String messagePurgeBroadcastPost = "&9AutoPurge Complete";
	@ConfigOption(path = "autorestart.restarting")
	public String messageAutoRestart = "&9Server is restarting";
	@ConfigOption(path = "autorestart.countdown")
	public String messageAutoRestartCountdown = "&9Server will restart in {SECONDS} seconds";
	@ConfigOption(path = "worldregen.kickmessage")
	public String messageWorldRegenKick = "&9Server is regenerating map, please come back later";
	@ConfigOption(path = "insufficentpermissions")
	public String messageInsufficientPermissions = "&cYou do not have access to that command.";

	@Override
	public File getFile() {
		return GlobalConstants.getMessageConfigPath();
	}

}
