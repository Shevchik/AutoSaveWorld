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

import autosaveworld.core.AutoSaveWorld;

public class AutoSaveConfigMSG {

	private AutoSaveConfig config;
	private AutoSaveWorld plugin;	
	public AutoSaveConfigMSG(AutoSaveWorld plugin, AutoSaveConfig config) {
		this.plugin = plugin;
		this.config = config;		
	}
	
	public ENDefaults enMessages = new ENDefaults(this);
	private LocaleLoader lLoader  = new LocaleLoader(this);
	
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
	
	public void loadmsg() 
	{
		File configfile;
		if (!config.switchtolangfile) {
			configfile = new File(plugin.constants.getConfigMSGPath());
		} else {
			configfile = new File(plugin.constants.getConfigMSGWithSuffix(config.langfilesuffix));
		}
		lLoader.loadLocaleMessegaes(configfile);
	}

}
