package autosaveworld.threads.worldregen;

import java.io.File;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.schematic.SchematicFormat;
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
	private boolean run = true;
	
	private boolean doregen = false;
	
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
				try {
				doWorldRegen();
				} catch (Exception e) {
					e.printStackTrace();
				}
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
	
	
	private void doWorldRegen() throws Exception
	{
		final World wtoregen = Bukkit.getWorld(worldtoregen);
		int taskid;
		
		//kick all player and deny them from join
		JListener jl = new JListener();
		Bukkit.getPluginManager().registerEvents(jl, plugin);
		taskid = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
		{
			public void run()
			{
				for (Player p : Bukkit.getOnlinePlayers())
				{
					p.kickPlayer("[AutoSaveWorld] server is regenerating map, please come back later");
				}
			}
		});
		while (Bukkit.getScheduler().isCurrentlyRunning(taskid) || Bukkit.getScheduler().isQueued(taskid))
		{
				Thread.sleep(1000);
		}
		
		//save WorldGuard buildings
		plugin.debug("Saving buildings");
		if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null)
		{
			plugin.debug("Saving wg regions to schematics");
			WorldGuardPlugin wg = (WorldGuardPlugin) plugin.getServer().getPluginManager().getPlugin("WorldGuard");
			final RegionManager m = wg.getRegionManager(wtoregen);
		    final SchematicFormat format = SchematicFormat.getFormats().iterator().next();
			final String schemfolder = "plugins/AutoSaveWorld/WorldRegenTemp/";
			new File(schemfolder).mkdirs();
				//save region to schematic
				for (final ProtectedRegion rg : m.getRegions().values()) {
					Runnable copypaste = new Runnable() {
						public void run(){
							try {
							plugin.debug("Saving WG Regions "+rg.getId()+" to schematic");
							//copy to clipboard
							EditSession es = new EditSession(new BukkitWorld(wtoregen),Integer.MAX_VALUE);
							Vector bvmin = rg.getMinimumPoint().toBlockPoint();
							Vector bvmax = rg.getMaximumPoint().toBlockPoint();
							Vector pos = bvmax;
							CuboidClipboard clipboard = new CuboidClipboard(
									bvmax.subtract(bvmin).add(new Vector(1, 1, 1)),
									bvmin, bvmin.subtract(pos)
							);
							clipboard.copy(es);
							//save to schematic
					        File schematic = new File(schemfolder + "ASW_schematic_WG_"+rg.getId());
					        format.save(clipboard, schematic);
					        plugin.debug("WG Region "+rg.getId()+" saved");
							} catch (Exception e)
							{
								e.printStackTrace();
							}
						}
					};
					taskid = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, copypaste);
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
				//will do this later.
		}
		
		//Shutdown server and delegate world removal to JVMShutdownHook
		
	}
	
	//antijoin listener
	class JListener implements Listener
	{
		@EventHandler
		public void onPlayerJoin(PlayerJoinEvent e)
		{
			e.getPlayer().kickPlayer("[AutoSaveWorld] server is regenerating map, please come back later");
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
