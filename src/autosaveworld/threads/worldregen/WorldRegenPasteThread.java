package autosaveworld.threads.worldregen;

import autosaveworld.config.AutoSaveConfig;
import autosaveworld.config.AutoSaveConfigMSG;
import autosaveworld.core.AutoSaveWorld;

public class WorldRegenPasteThread extends Thread {

	private AutoSaveWorld plugin = null;
	@SuppressWarnings("unused")
	private AutoSaveConfig config;
	@SuppressWarnings("unused")
	private AutoSaveConfigMSG configmsg;
	
	public WorldRegenPasteThread(AutoSaveWorld plugin, AutoSaveConfig config, AutoSaveConfigMSG configmsg)
	{
		this.plugin = plugin;
		this.config = config;
		this.configmsg = configmsg;
	};
	
	public void run()
	{
		
		
		
		
		
		//restart
		plugin.worldregenfinished = true;
		plugin.autorestartThread.startrestart();
	}
}
