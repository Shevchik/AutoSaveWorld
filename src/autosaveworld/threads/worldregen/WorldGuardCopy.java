package autosaveworld.threads.worldregen;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.World;

import autosaveworld.core.AutoSaveWorld;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.schematic.SchematicFormat;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class WorldGuardCopy {

	private AutoSaveWorld plugin;
	private World wtoregen;
	
	public WorldGuardCopy(AutoSaveWorld plugin, String worldtoregen)
	{
		this.plugin = plugin;
		this.wtoregen = Bukkit.getWorld(worldtoregen);
	}
	
	
	private int taskid;
	
	public void copyAllToSchematics()
	{
		plugin.debug("Saving wg regions to schematics");
		WorldGuardPlugin wg = (WorldGuardPlugin) plugin.getServer().getPluginManager().getPlugin("WorldGuard");
		final RegionManager m = wg.getRegionManager(wtoregen);
	    final SchematicFormat format = SchematicFormat.getFormats().iterator().next();
		final String schemfolder = "plugins/AutoSaveWorld/WorldRegenTemp/WG/";
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
				        File schematic = new File(schemfolder + rg.getId());
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
}
