package autosaveworld.config;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import autosaveworld.config.localefiles.LocaleFiles;
import autosaveworld.core.AutoSaveWorld;
import autosaveworld.core.GlobalConstants;
import autosaveworld.core.logging.MessageLogger;

public class LocaleChanger {

	private AutoSaveWorld plugin;
	private AutoSaveWorldConfigMSG configmsg;
	public LocaleChanger(AutoSaveWorld plugin, AutoSaveWorldConfigMSG configmsg) {
		this.plugin = plugin;
		this.configmsg = configmsg;
	}

	//available locales
	public List<String> getAvailableLocales() {	
		List<String> locales = new LinkedList<String>();
		try {
			//add additional locales based on files in the jar.
			final ZipFile zipFile = new ZipFile(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().getFile());
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry ze = entries.nextElement();
				if (!ze.isDirectory() && ze.getName().contains("/")) {
					String pathname = ze.getName().substring(0, ze.getName().lastIndexOf("/"));
					String filename = ze.getName().substring(ze.getName().lastIndexOf("/") + 1, ze.getName().length());
					if (pathname.equalsIgnoreCase(LocaleFiles.getPackageName())) {
						if (filename.endsWith(".yml") && filename.contains("_")) {
							locales.add(filename.substring(0, filename.length() - 4).split("[_]")[1]);
						}
					}
				}
			}
			zipFile.close();
		} catch (Exception e) {
			locales.add("Error occured while scanning for available locales");
		}
		return locales;
	}

	public void loadLocale(String locale) {
		//load messages file from package
		MessageLogger.debug("switching to "+locale);
		loadLocaleFile(locale);
		//now load it
		MessageLogger.debug("loading configs");
		configmsg.loadmsg();
	}

	//load needed locale file
	private void loadLocaleFile(String locale) {
		try {
			InputStream is = getClass().getResourceAsStream("localefiles/configmsg_"+locale+".yml");
			Path file = new File(GlobalConstants.getConfigMSGPath()).toPath();
			Files.copy(is, file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
