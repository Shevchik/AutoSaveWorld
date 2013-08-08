package autosaveworld.threads.worldregen.griefprevention;

import java.io.File;
import java.lang.reflect.Field;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimArray;
import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

import org.bukkit.Bukkit;
import org.bukkit.World;

import autosaveworld.core.AutoSaveWorld;
import autosaveworld.threads.worldregen.WorldRegenConstants;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.schematic.SchematicFormat;

public class GPPaste {

	private AutoSaveWorld plugin;
	private World wtopaste;
	
	public GPPaste(AutoSaveWorld plugin, String worldtopasteto)
	{
		this.plugin = plugin;
		this.wtopaste = Bukkit.getWorld(worldtopasteto);
	}
	
	
	private int taskid;
	final SchematicFormat format = SchematicFormat.getFormats().iterator().next();
	final String schemfolder = WorldRegenConstants.getGPTempFolder();
	
	public void pasteAllFromSchematics()
	{
		plugin.debug("Pasting GP regions from schematics");
		
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
			Bukkit.getLogger().severe("[AutoSaveWorld] Failed to access GriefPrevntion database. GP paste cancelled");
			return;
		}

		for (int i = 0; i<ca.size(); i++)
		{
			Claim claim = ca.get(i);
			pasteGPRegion(claim);
		}
	}
	
	
	private void pasteGPRegion(final Claim claim)
	{
		Runnable copypaste = new Runnable() 
		{
			public void run() 
			{
				try {
					plugin.debug("Pasting GP region "+claim.getID()+" from schematics");
					//load from schematic to clipboard
					EditSession es = new EditSession(new BukkitWorld(wtopaste),Integer.MAX_VALUE);
					File f = new File(schemfolder+claim.getID());
					CuboidClipboard cc = format.load(f);
					//paste clipboard at origin
					cc.place(es, cc.getOrigin(), false);
					plugin.debug("Pasted GP region "+claim.getID()+" from schematics");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		taskid = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, copypaste);
		while (Bukkit.getScheduler().isCurrentlyRunning(taskid) || Bukkit.getScheduler().isQueued(taskid)) {
			try {Thread.sleep(100);} catch (InterruptedException e){e.printStackTrace();}
		}
	}
	
}
