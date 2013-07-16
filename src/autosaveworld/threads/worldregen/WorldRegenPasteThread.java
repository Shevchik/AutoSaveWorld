package autosaveworld.threads.worldregen;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import autosaveworld.config.AutoSaveConfig;
import autosaveworld.config.AutoSaveConfigMSG;
import autosaveworld.core.AutoSaveWorld;

public class WorldRegenPasteThread extends Thread {

	private AutoSaveWorld plugin = null;
	@SuppressWarnings("unused")
	private AutoSaveConfig config;
	@SuppressWarnings("unused")
	private AutoSaveConfigMSG configmsg;

	private String worldtopasteto;

	public long loaded = 0;
	public WorldRegenPasteThread(AutoSaveWorld plugin, AutoSaveConfig config,
			AutoSaveConfigMSG configmsg) {
		this.plugin = plugin;
		this.config = config;
		this.configmsg = configmsg;
	};

	public void run() {
		try {
			
			//create task that will tell us that server is loaded
			int ltask = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
			{
				public void run()
				{
					loaded = System.currentTimeMillis();
				}
			});
			
			FileConfiguration cfg = YamlConfiguration.loadConfiguration(new File("plugins/AutoSaveWorld/WorldRegenTemp/wname.yml"));
			worldtopasteto = cfg.getString("wname");

			// wait until world is loaded
			while (loaded == 0) {
				Thread.sleep(1000);
			}
			//cancel no longet needed task
			Bukkit.getScheduler().cancelTask(ltask);
			
			
			// paste WG buildings
			if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
				new WorldGuardPaste(plugin, worldtopasteto).pasteAllFromSchematics();
			}
			
			if (Bukkit.getPluginManager().getPlugin("Factions") != null) {
				
			}

			// restart
			plugin.worldregenfinished = true;
			plugin.autorestartThread.startrestart();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
}
