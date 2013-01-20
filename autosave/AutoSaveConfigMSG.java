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
	public AutoSaveConfigMSG(FileConfiguration configmsg) {
		this.configmsg = configmsg;
	}
	//Locale
	protected String Locale = "en";
	// Messages
	protected String messageBroadcastPre = "&9AutoSaving";
	protected String messageBroadcastPost = "&9AutoSave Complete";
	protected String messageStatusFail = "&9AutoSave has stopped, check the server logs for more info";
	protected String messageStatusNotRun = "&9AutoSave is running but has not yet saved.";
	protected String messageStatusSuccess = "&9AutoSave is running and last saved at ${DATE}.";
	protected String messageStatusOff = "&9AutoSave is not running (disabled)";
	protected String messageInsufficientPermissions = "&cYou do not have access to that command.";
	protected String messageStopping = "&9AutoSave Stopping";
	protected String messageStarting = "&9AutoSave Starting";
	protected String messageInfoNaN = "&cYou must enter a valid number, ex: 300";
	protected String messageInfoChangeSuccess = "&9${VARIABLE} has been updated.";
	protected String messageInfoLookup = "&9${VARIABLE} is ${VALUE}";
	protected String messageInfoListLookup = "&9${VARIABLE} is set to [${VALUE}]";
	protected String messageInfoInvalid = "&cYou must enter a valid setting (${VALIDSETTINGS})";
	protected String messageVersion = "&9AutoSave v${VERSION}, Instance ${UUID}";
	protected String messageWarning = "&9Warning, AutoSave will commence soon.";
	protected String messageBroadcastBackupPre = "&9AutoBackuping";
	protected String messageBroadcastBackupPost = "&9AutoBackup Complete";
	protected String messageBackupWarning = "&9Warning, AutoBackup will commence soon";
	
	public void loadmsg() {
		configmsg = YamlConfiguration.loadConfiguration(new File("plugins/AutoSaveWorld/configmsg.yml"));
		Locale = configmsg.getString("locale",Locale);
		messageBroadcastPre =configmsg.get(Locale+".broadcast.pre", messageBroadcastPre).toString();
		messageBroadcastPost =configmsg.get(Locale+".broadcast.post", messageBroadcastPost).toString();
		messageBroadcastBackupPre =configmsg.get(Locale+".broadcastbackup.pre", messageBroadcastBackupPre).toString();
		messageBroadcastBackupPost =configmsg.get(Locale+".broadcastbackup.post", messageBroadcastBackupPost).toString();
		messageStatusFail =configmsg.get(Locale+".status.fail", messageStatusFail).toString();
		messageStatusNotRun =configmsg.get(Locale+".status.notrun", messageStatusNotRun).toString();
		messageStatusSuccess =configmsg.get(Locale+".status.success", messageStatusSuccess).toString();
		messageStatusOff =configmsg.get(Locale+".status.off", messageStatusOff).toString();
		messageInsufficientPermissions =configmsg.get(Locale+".insufficentpermissions", messageInsufficientPermissions).toString();
		messageStopping =configmsg.get(Locale+".stopping", messageStopping).toString();
		messageStarting =configmsg.get(Locale+".starting", messageStarting).toString();
		messageInfoNaN =configmsg.get(Locale+".info.nan", messageInfoNaN).toString();
		messageInfoChangeSuccess =configmsg.get(Locale+".info.changesuccess", messageInfoChangeSuccess).toString();
		messageInfoLookup =configmsg.get(Locale+".infolookup", messageInfoLookup).toString();
		messageInfoListLookup =configmsg.get(Locale+".infolistlookup", messageInfoListLookup).toString();
		messageInfoInvalid =configmsg.get(Locale+".infoinvalid", messageInfoInvalid).toString();
		messageVersion =configmsg.get(Locale+".version", messageVersion).toString();
		messageWarning =configmsg.get(Locale+".warning", messageWarning).toString(); 
		messageBackupWarning =configmsg.get(Locale+".warningbackup", messageBackupWarning).toString();
		configmsg = new YamlConfiguration();
		configmsg.set("Locale", Locale);
		configmsg.set(Locale+".broadcast.pre", messageBroadcastPre);
		configmsg.set(Locale+".broadcast.post", messageBroadcastPost);
		configmsg.set(Locale+".broadcastbackup.pre", messageBroadcastBackupPre);
		configmsg.set(Locale+".broadcastbackup.post", messageBroadcastBackupPost);
		configmsg.set(Locale+".status.fail", messageStatusFail);
		configmsg.set(Locale+".status.notrun", messageStatusNotRun);
		configmsg.set(Locale+".status.success", messageStatusSuccess);
		configmsg.set(Locale+".status.off", messageStatusOff);
		configmsg.set(Locale+".insufficentpermissions", messageInsufficientPermissions);
		configmsg.set(Locale+".stopping", messageStopping);
		configmsg.set(Locale+".starting", messageStarting);
		configmsg.set(Locale+".info.nan", messageInfoNaN);
		configmsg.set(Locale+".info.changesuccess", messageInfoChangeSuccess);
		configmsg.set(Locale+".infolookup", messageInfoLookup);
		configmsg.set(Locale+".infolistlookup", messageInfoListLookup);
		configmsg.set(Locale+".infoinvalid", messageInfoInvalid);
		configmsg.set(Locale+".version", messageVersion);
		configmsg.set(Locale+".warning", messageWarning);
		configmsg.set(Locale+".warningbackup", messageBackupWarning);

		
		try {
			configmsg.save(new File("plugins/AutoSaveWorld/configmsg.yml"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	
	
}
