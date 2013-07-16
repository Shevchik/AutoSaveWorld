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
import java.util.List;

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
		locales.add("ru");
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
