package autosaveworld.threads.worldregen;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.schematic.SchematicFormat;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import autosaveworld.config.AutoSaveConfig;
import autosaveworld.config.AutoSaveConfigMSG;
import autosaveworld.core.AutoSaveWorld;

public class WorldRegenPasteThread extends Thread {

	private AutoSaveWorld plugin = null;
	@SuppressWarnings("unused")
	private AutoSaveConfig config;
	@SuppressWarnings("unused")
	private AutoSaveConfigMSG configmsg;

	private String worldtopaste;
	private int taskid;
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
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
			{
				public void run()
				{
					loaded = System.currentTimeMillis();
				}
			});
			
			FileConfiguration cfg = YamlConfiguration.loadConfiguration(new File("plugins/AutoSaveWorld/WorldRegenTemp/wname.yml"));
			worldtopaste = cfg.getString("wname");

			// wait until world is loaded
			while (loaded == 0) {
				Thread.sleep(1000);
			}

			final World wtopaste = Bukkit.getWorld(worldtopaste);

			// paste WG buildings
			if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
				plugin.debug("Pasting wg regions from schematics");
				WorldGuardPlugin wg = (WorldGuardPlugin) plugin.getServer().getPluginManager().getPlugin("WorldGuard");
				final RegionManager m = wg.getRegionManager(wtopaste);
				final SchematicFormat format = SchematicFormat.getFormats().iterator().next();
				final String schemfolder = "plugins/AutoSaveWorld/WorldRegenTemp/WG/";
				new File(schemfolder).mkdirs();
				// restore region to schematic
				for (final ProtectedRegion rg : m.getRegions().values()) {
					Runnable copypaste = new Runnable() {
						public void run() {
							try {
								plugin.debug("Pasting region "+rg.getId()+" from schematics");
								EditSession es = new EditSession(new BukkitWorld(wtopaste),Integer.MAX_VALUE);
								File f = new File("plugins/AutoSaveWorld/WorldRegenTemp/WG/"+rg.getId());
								CuboidClipboard cc = format.load(f);
								cc.place(es, cc.getOrigin(), false);
								plugin.debug("Pasted regions "+rg.getId()+" from schematics");
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					};
					taskid = Bukkit.getScheduler().scheduleSyncDelayedTask(
							plugin, copypaste);
					while (Bukkit.getScheduler().isCurrentlyRunning(taskid) || Bukkit.getScheduler().isQueued(taskid)) {
						try {Thread.sleep(100);} catch (InterruptedException e){e.printStackTrace();}
					}
				}
				//delete Wg folder firectory
				deleteDirectory(new File(schemfolder));
			}

			// restart
			plugin.worldregenfinished = true;
			plugin.autorestartThread.startrestart();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public void deleteDirectory(File file)
	  {
	    if(!file.exists())
	      return;
	    if(file.isDirectory())
	    {
	      for(File f : file.listFiles())
	        deleteDirectory(f);
	      file.delete();
	    }
	    else
	    {
	      file.delete();
	    }
	  }
}
