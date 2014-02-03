package autosaveworld.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import autosaveworld.core.AutoSaveWorld;

public class LocaleChanger {


	private AutoSaveWorld plugin;
	private AutoSaveConfigMSG configmsg;
	public LocaleChanger(AutoSaveWorld plugin, AutoSaveConfigMSG configmsg) {
		this.plugin = plugin;
		this.configmsg = configmsg;
	}


	//available locales
	public List<String> getAvailableLocales()
	{
		List<String> locales = new ArrayList<String>();
		try {
			//add additional locales based on files in the jar.
			final ZipFile zipFile = new ZipFile(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().getFile());
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
		} catch (Exception e) {}
		return locales;
	}

	public void loadLocale(String locale)
	{
		//load messages file from package
		plugin.debug("switching to "+locale);
		loadLocaleFile(locale);
		//now load it
		plugin.debug("loading configs");
		configmsg.loadmsg();
	}

	//load needed locale file
	private void loadLocaleFile(String locale)
	{
		try {
			InputStream in = getClass().getResourceAsStream("localefiles/configmsg_"+locale+".yml");
			OutputStream out = new FileOutputStream(new File(plugin.constants.getConfigMSGPath()));

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
