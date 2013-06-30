package autosaveworld.threads.worldregen;

import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.generator.ChunkGenerator;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import autosaveworld.config.AutoSaveConfig;
import autosaveworld.config.AutoSaveConfigMSG;
import autosaveworld.core.AutoSaveWorld;

public class WorldRegen extends Thread {

	protected final Logger log = Bukkit.getLogger();
	
	
	private AutoSaveWorld plugin = null;
	private AutoSaveConfig config;
	private AutoSaveConfigMSG configmsg;
	private volatile boolean run = true;
	
	public volatile boolean doregen = false;
	public volatile boolean worldcreated = false;
	
	private String worldtoregen = "";
	
	public WorldRegen(AutoSaveWorld plugin, AutoSaveConfig config,
			AutoSaveConfigMSG configmsg) {
		this.plugin = plugin;
		this.config = config;
		this.configmsg = configmsg;
	}
	
	// Allows for the thread to naturally exit if value is false
	public void stopThread() {
		this.run = false;
	}
	
	public void startworldregen(String worldname) {
		doregen = true;
		this.worldtoregen = worldname;
	}
	
	
	public void run()
	{
		log.info("[%s] WorldRegenThread Started");
		
		Thread.currentThread().setName("AutoSaveWorld WorldRegenThread");
		
		while (run)
		{
			if (doregen)
			{
				doWorldRegen();
				doregen = false;
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	private void doWorldRegen()
	{
		//create world
		worldcreated = false;
		final Long wseed = Bukkit.getWorld(worldtoregen).getSeed();
		final ChunkGenerator wgen = Bukkit.getWorld(worldtoregen).getGenerator();
		final Environment wenv = Bukkit.getWorld(worldtoregen).getEnvironment();
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
		{
			public void run()
			{
				WorldCreator wc = new WorldCreator("AutoSaveWorld_world_regen_"+worldtoregen);
				wc.seed(wseed);
				wc.generator(wgen);
				wc.environment(wenv);
				Bukkit.getServer().createWorld(wc);
				worldcreated = true;
			}
			
		});
		//wait for world creation done
		while (!worldcreated)
		{
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//save WorldGuard buildings
		if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null)
		{
			WorldGuardPlugin wg = (WorldGuardPlugin) plugin.getServer().getPluginManager().getPlugin("WorldGuard");
			List<World> worldlist = Bukkit.getWorlds();
				World wtoregen = Bukkit.getWorld(worldtoregen);
				World wclipboard = Bukkit.getWorld("AutoSaveWorld_world_regen_"+worldtoregen);
				final RegionManager m = wg.getRegionManager(wtoregen);
				//get region
				for (ProtectedRegion rg : m.getRegions().values()) {
					BlockVector bvmin = rg.getMinimumPoint();
					BlockVector bvmax = rg.getMinimumPoint();
					//copy&paste everything to the new world
					
					
				}
		}
	}
}
