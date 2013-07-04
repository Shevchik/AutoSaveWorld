package autosaveworld.threads.worldregen;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
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
			plugin.debug("Copy-pasting wg regions to clipboard world");
			WorldGuardPlugin wg = (WorldGuardPlugin) plugin.getServer().getPluginManager().getPlugin("WorldGuard");
				final RegionManager m = wg.getRegionManager(wtoregen);
				final LocalSession ls = new LocalSession(WorldEdit.getInstance().getConfiguration());
				//get region
				for (final ProtectedRegion rg : m.getRegions().values()) {
					Runnable copypaste = new Runnable() {
						public void run(){
							//save to schematic here
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
				//will do this later.
		}
		
		//wipe previous map
		//Unload all chunks
		taskid = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
		{
			public void run()
			{
				for (Chunk c : wtoregen.getLoadedChunks())
				{
					c.unload();
				}
			}
		});
		while (Bukkit.getScheduler().isCurrentlyRunning(taskid) || Bukkit.getScheduler().isQueued(taskid))
		{
				Thread.sleep(1000);	
		}
		//remove old region files
		String oldwregionspath = wtoregen.getWorldFolder().getCanonicalPath()+File.separator+"region";
		deleteDirectory(new File(oldwregionspath));
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
