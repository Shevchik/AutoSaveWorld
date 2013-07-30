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
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import autosaveworld.core.AutoSaveWorld;

public class LocaleLoader {
	public LocaleLoader(AutoSaveWorld plugin,AutoSaveConfig config, AutoSaveConfigMSG configmsg) {
		this.plugin = plugin;
		this.config = config;
		this.configmsg = configmsg;
	}
	private AutoSaveWorld plugin;
	private AutoSaveConfig config;
	private AutoSaveConfigMSG configmsg;
	
	
	//availible locales
	public List<String> getAvailableLocales()
	{
		List<String> locales = new ArrayList<String>(Arrays.asList("en"));
		
		try {
		//add additional locales based on files in the jar.
    	final ZipFile zipFile = new ZipFile(AutoSaveWorld.class.getProtectionDomain().getCodeSource().getLocation().getFile());
    	Enumeration<? extends ZipEntry> entries = zipFile.entries();
    	while (entries.hasMoreElements())
    	{
    		ZipEntry ze = entries.nextElement();
    		if (!ze.isDirectory())
    		{
    			if (ze.getName().contains("localefiles"))
    			{
    				String lname = new File(ze.getName()).getName();
    				lname = lname.split("[_]")[1];
    				lname = lname.split("[.]")[0];
    				locales.add(lname);
    			}
    		}
    	}
    	zipFile.close();
		} catch (Exception e)
		{
		}
		
   		return locales;
	}
	
	public void loadLocale(String locale)
	{
		//chose needed locale
		if (locale.equalsIgnoreCase("en"))
		{
			//if it's en (default locale) we will recreate default messages file
			new File("plugins/AutoSaveWorld/configmsg.yml").delete();
			plugin.debug("switching to en");
			config.switchtolangfile = false;
			configmsg.messageSaveBroadcastPre = "&9AutoSaving";
			configmsg.messageSaveBroadcastPost = "&9AutoSave Complete";
			configmsg.messageInsufficientPermissions = "&cYou do not have access to that command.";
			configmsg.messageBackupBroadcastPre = "&9AutoBackuping";
			configmsg.messageBackupBroadcastPost = "&9AutoBackup Complete";
			configmsg.messagePurgeBroadcastPre = "&9AutoPurging";
			configmsg.messagePurgeBroadcastPost = "&9AutoPurge Complete";
			configmsg.messageAutoRestart = "&9Server is restarting";
			configmsg.messageAutoRestartCountdown = "&9Server will restart in {SECONDS} seconds";
			configmsg.messageWorldRegenKick = "&9Server is regenerating map, please come back later";
		} else 
		{
			//if it is other locale we will load messages file from package
			plugin.debug("switching to "+locale);
			config.switchtolangfile = true;
			config.langfilesuffix = locale;
			loadLocaleFile(locale);
		}
		//now load it
		plugin.debug("loading configs");
		config.save();
		config.load();
		configmsg.loadmsg();
		
	}
	
	//load needed locale file
	private void loadLocaleFile(String locale)
	{
		try {
			InputStream in = getClass().getResourceAsStream("localefiles/configmsg_"+locale+".yml");
			OutputStream out = new FileOutputStream(new File("plugins/AutoSaveWorld/configmsg_"+locale+".yml"));

			byte[] buf = new byte[4096];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		} catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	

}
