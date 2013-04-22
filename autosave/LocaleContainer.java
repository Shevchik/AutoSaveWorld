package autosave;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class LocaleContainer {
	LocaleContainer(AutoSave plugin,AutoSaveConfig config, AutoSaveConfigMSG configmsg) {
		this.plugin = plugin;
		this.config = config;
		this.configmsg = configmsg;
	}
	private AutoSave plugin;
	private AutoSaveConfig config;
	private AutoSaveConfigMSG configmsg;
	
	
	//availible locales
	public List<String> getAvailibleLocales()
	{
				return Arrays.asList("en","ru");
	}
	
	public void loadLocale(String locale)
	{
		//chose needed locale
		if (locale.equalsIgnoreCase("en"))
		{
			plugin.debug("switching to en");
			config.switchtolangfile = false;

		} else 
		if (locale.equalsIgnoreCase("ru"))
		{
			plugin.debug("switching to ru");
			config.switchtolangfile = true;
			config.langfilesuffix = "ru";
			createLocaleFile("ru");
		}
		//now load it
		plugin.debug("loading configs");
		config.save();
		config.load();
		configmsg.loadmsg();
		
	}
	
	//create needed locale file
	private void createLocaleFile(String locale)
	{
		FileConfiguration localefile = new YamlConfiguration();
		if (locale.equalsIgnoreCase("ru"))
			{
			plugin.debug("loading ru locale files");
			localefile.set("broadcast.pre", "&9Автосохранение сервера");
			localefile.set("broadcast.post", "&9Сохранение завершено");
			localefile.set("broadcastbackup.pre", "&9Делаем автобэкап");
			localefile.set("broadcastbackup.post", "&9Автобэкап сделан");
			localefile.set("broadcastpurge.pre", "&9Автоочистка сервера");
			localefile.set("broadcastpurge.post", "&9Очистка завершена");
			localefile.set("warning.save", "&9Скоро начнётся автосохранение сервера");
			localefile.set("warning.backup", "&9Скоро начнётся автобэкап сервера");
			localefile.set("insufficentpermissions", "&9У вас нет доступа к этой команде");
			localefile.set("autorestart.restarting","&9Сервер перезагружается");
			}
		try {localefile.save(new File("plugins/AutoSaveWorld/configmsg_"+locale+".yml"));} catch (IOException e) {e.printStackTrace();}
	}
	

}
