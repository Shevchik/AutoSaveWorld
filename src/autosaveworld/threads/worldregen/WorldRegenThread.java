package autosaveworld.threads.worldregen;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.generator.ChunkGenerator;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import autosaveworld.config.AutoSaveConfig;
import autosaveworld.config.AutoSaveConfigMSG;
import autosaveworld.core.AutoSaveWorld;

public class WorldRegenThread extends Thread {

	protected final Logger log = Bukkit.getLogger();
	
	
	private AutoSaveWorld plugin = null;
	private AutoSaveConfig config;
	@SuppressWarnings("unused")
	private AutoSaveConfigMSG configmsg;
	private volatile boolean run = true;
	
	private volatile boolean doregen = false;
	
	private String worldtoregen = "";
	
	public WorldRegenThread(AutoSaveWorld plugin, AutoSaveConfig config,
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
	
	public boolean isRegenerationInProcess()
	{
		return doregen;
	}
	
	
	public void run()
	{
		log.info("[AutoSaveWorld] WorldRegenThread Started");
		
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
				e.printStackTrace();
			}
		}
		
		if (config.varDebug) {log.info("[AutoSaveWorld] Graceful quit of WorldRegenThread");}
	}
	
	
	private void doWorldRegen()
	{
		//create world
		plugin.debug("Creating clipboard world");
		final Long wseed = Bukkit.getWorld(worldtoregen).getSeed();
		final ChunkGenerator wgen = Bukkit.getWorld(worldtoregen).getGenerator();
		final Environment wenv = Bukkit.getWorld(worldtoregen).getEnvironment();
		int taskid = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
		{
			public void run()
			{
				WorldCreator wc = new WorldCreator("AutoSaveWorld_world_regen_"+worldtoregen);
				wc.seed(wseed);
				wc.generator(wgen);
				wc.environment(wenv);
				Bukkit.getServer().createWorld(wc);
			}
			
		});
		//wait for world creation done
		while (Bukkit.getScheduler().isCurrentlyRunning(taskid) || Bukkit.getScheduler().isQueued(taskid))
		{
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		plugin.debug("Saving buildings");
		//save WorldGuard buildings
		if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null)
		{
			plugin.debug("Copy-pasting wg regions to clipboard world");
			WorldGuardPlugin wg = (WorldGuardPlugin) plugin.getServer().getPluginManager().getPlugin("WorldGuard");
				final World wtoregen = Bukkit.getWorld(worldtoregen);
				final World wclipboard = Bukkit.getWorld("AutoSaveWorld_world_regen_"+worldtoregen);
				final RegionManager m = wg.getRegionManager(wtoregen);
				final LocalSession ls = new LocalSession(WorldEdit.getInstance().getConfiguration());
				//get region
				for (final ProtectedRegion rg : m.getRegions().values()) {
					Runnable copypaste = new Runnable() {
						public void run(){
							//copy&paste everything to the new world
							Vector bvmin = rg.getMinimumPoint().toBlockPoint();
							Vector bvmax = rg.getMinimumPoint().toBlockPoint();
							Vector pos = bvmax;
							CuboidClipboard clipboard = new CuboidClipboard(
									bvmax.subtract(bvmin).add(new Vector(1, 1, 1)),
									bvmin, bvmin.subtract(pos)
			        		);
							EditSession es = new EditSession(new BukkitWorld(wtoregen), Integer.MAX_VALUE);
							clipboard.copy(es);
							ls.setClipboard(clipboard);
							EditSession esn = new EditSession(new BukkitWorld(wclipboard),Integer.MAX_VALUE);
							try {
								ls.getClipboard().paste(esn, pos, false, false);
							} catch (MaxChangedBlocksException e) {
								e.printStackTrace();
							} catch (EmptyClipboardException e) {
								e.printStackTrace();
							}	
							plugin.debug("Region "+rg.getId()+" saved to clipboard map");
						}
					};
					taskid = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, copypaste);
					//wait for copy&paste finished
					while (Bukkit.getScheduler().isCurrentlyRunning(taskid) || Bukkit.getScheduler().isQueued(taskid))
					{
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
		}
		
		//save Factions homes
		if (Bukkit.getPluginManager().getPlugin("Factions") != null)
		{

		}
		
	}
	
}
