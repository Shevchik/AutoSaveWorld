package autosaveworld.threads.worldregen.towny;

import java.io.File;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import autosaveworld.core.AutoSaveWorld;
import autosaveworld.threads.worldregen.WorldRegenCopyThread;

import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownyUniverse;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.bukkit.BukkitUtil;

public class TownyCopy {

	private AutoSaveWorld plugin;
	private WorldRegenCopyThread wrthread;
	private World wtoregen;
	public TownyCopy(AutoSaveWorld plugin, WorldRegenCopyThread wrthread, String worldtoregen)
	{
		this.plugin = plugin;
		this.wrthread = wrthread;
		this.wtoregen = Bukkit.getWorld(worldtoregen);
	}

	public void copyAllToSchematics()
	{
		plugin.debug("Saving Towny towns to schematics");

		new File(plugin.constants.getTownyTempFolder()).mkdirs();

		try
		{
			List<Town> towns = TownyUniverse.getDataSource().getWorld(wtoregen.getName()).getTowns();
			for (Town town : towns)
			{
				List<TownBlock> tblocks = town.getTownBlocks();
				if (tblocks.size() > 0)
				{
					//find min and max coords
					int xmin = tblocks.get(0).getX();
					int xmax = tblocks.get(0).getX();
					int zmin = tblocks.get(0).getZ();
					int zmax = tblocks.get(0).getZ();
					for (TownBlock tb : tblocks)
					{
						if (tb.getWorld().getName().equals(wtoregen.getName()))
						{
							xmax = Math.max(xmax, tb.getX());
							xmin = Math.min(xmin, tb.getX());
							zmax = Math.max(zmax, tb.getZ());
							zmin = Math.min(zmin, tb.getZ());
						}
					}
					Vector bvmin = BukkitUtil.toVector(new Location(wtoregen, xmin, 0, zmin));
					Vector bvmax = BukkitUtil.toVector(new Location(wtoregen, xmax, wtoregen.getMaxHeight(), xmax));
					//save
					plugin.debug("Saving Towny town "+town.getName()+" to schematic");
					wrthread.getSchematicOperations().saveToSchematic(plugin.constants.getWGTempFolder()+town.getName(), wtoregen, bvmin, bvmax);
					plugin.debug("Towny town "+town.getName()+" saved");
				}
			}
		}
		catch (NotRegisteredException e)
		{
			e.printStackTrace();
		}
	}

}
