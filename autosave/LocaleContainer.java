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
		FileConfiguration loacalefile = new YamlConfiguration();
		if (locale.equalsIgnoreCase("ru"))
			{
			plugin.debug("loading ru locale files");
			loacalefile.set("broadcast.pre", "&9Автосохранение сервера");
			loacalefile.set("broadcast.post", "&9Сохранение завершено");
			loacalefile.set("broadcastbackup.pre", "&9Делаем автобэкап");
			loacalefile.set("broadcastbackup.post", "&9Автобэкап сделан");
			loacalefile.set("broadcastpurge.pre", "&9Автоочистка сервера");
			loacalefile.set("broadcastpurge.post", "&9Очистка завершена");
			loacalefile.set("warning.save", "&9Скоро начнётся автосохранение сервера");
			loacalefile.set("warning.backup", "&9Скоро начнётся автобэкап сервера");
			loacalefile.set("insufficentpermissions", "&9У вас нет доступа к этой команде");
			}
		try {loacalefile.save(new File("plugins/AutoSaveWorld/configmsg_"+locale+".yml"));} catch (IOException e) {e.printStackTrace();}
	}
	

}
