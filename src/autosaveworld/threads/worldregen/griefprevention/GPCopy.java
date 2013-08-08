package autosaveworld.threads.worldregen.griefprevention;

import java.io.File;
import java.lang.reflect.Field;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimArray;
import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.schematic.SchematicFormat;
import com.sk89q.worldguard.bukkit.BukkitUtil;

import autosaveworld.core.AutoSaveWorld;
import autosaveworld.threads.worldregen.WorldRegenConstants;

public class GPCopy {

	private AutoSaveWorld plugin;
	private World wtoregen;
	
	public GPCopy(AutoSaveWorld plugin, String worldtoregen)
	{
		this.plugin = plugin;
		this.wtoregen = Bukkit.getWorld(worldtoregen);
	}
	
	private int taskid;
    final SchematicFormat format = SchematicFormat.getFormats().iterator().next();
	final String schemfolder = WorldRegenConstants.getGPTempFolder();
	
	public void copyAllToSchematics()
	{
		plugin.debug("Saving griefprevention regions to schematics");
		
		GriefPrevention gp = (GriefPrevention) Bukkit.getPluginManager().getPlugin("GriefPrevention"); 
		ClaimArray ca = null;
		try {
            Field fld = DataStore.class.getDeclaredField("claims");
            fld.setAccessible(true);
            Object o = fld.get(gp.dataStore);
            ca = (ClaimArray) o;
		} 
		catch (Exception e)
		{
			e.printStackTrace();
			Bukkit.getLogger().severe("[AutoSaveWorld] Failed to access GriefPrevntion database. GP save cancelled");
			return;
		}
		new File(schemfolder).mkdirs();

		for (int i = 0; i<ca.size(); i++)
		{
			Claim claim = ca.get(i);
			saveGPRegion(claim);
		}
	}
	
	
	private void saveGPRegion(final Claim claim)
	{
		Runnable copypaste = new Runnable() 
		{
			public void run()
			{
				try {
				plugin.debug("Saving GP Region "+claim.getID()+" to schematic");
				//copy to clipboard
				EditSession es = new EditSession(new BukkitWorld(wtoregen),Integer.MAX_VALUE);
				double xmin = claim.getLesserBoundaryCorner().getX();
				double zmin = claim.getLesserBoundaryCorner().getZ();
				double xmax = claim.getGreaterBoundaryCorner().getX();
				double zmax = claim.getGreaterBoundaryCorner().getZ();
				Vector bvmin = BukkitUtil.toVector(
						new Location(
								wtoregen,
								xmin,
								0,
								zmin
						)
				);
				Vector bvmax = BukkitUtil.toVector(
						new Location(
								wtoregen,
								xmax,
								wtoregen.getMaxHeight(),
								zmax
						)
				);
				Vector pos = bvmax;
				CuboidClipboard clipboard = new CuboidClipboard(
						bvmax.subtract(bvmin).add(new Vector(1, 1, 1)),
						bvmin, bvmin.subtract(pos)
				);
				clipboard.copy(es);
				//save to schematic
		        File schematic = new File(schemfolder + claim.getID());
		        format.save(clipboard, schematic);
		        plugin.debug("GP Region "+claim.getID()+" saved");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		taskid = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, copypaste);
		while (Bukkit.getScheduler().isCurrentlyRunning(taskid) || Bukkit.getScheduler().isQueued(taskid))
		{
			try {Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace();}
		}
	}
	
}
