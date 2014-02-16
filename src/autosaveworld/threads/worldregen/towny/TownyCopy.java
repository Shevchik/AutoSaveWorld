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
		try
		{
			plugin.debug("Saving Towny towns to schematics");

			new File(plugin.constants.getTownyTempFolder()).mkdirs();
			
			List<Town> towns = TownyUniverse.getDataSource().getWorld(wtoregen.getName()).getTowns();
			for (Town town : towns)
			{
				List<TownBlock> tblocks = town.getTownBlocks();
				if (tblocks.size() > 0)
				{
					plugin.debug("Saving town claim "+town.getName()+" to schematic");
					for (TownBlock tb : tblocks)
					{
						if (tb.getWorld().getName().equals(wtoregen.getName()))
						{
							//create temp folder for town
							new File(plugin.constants.getTownyTempFolder()+town.getName()).mkdirs();
							//get coords
							final int xcoord = tb.getX();
							final int zcoord = tb.getZ();
							final Vector bvmin = BukkitUtil.toVector(
								new Location(
									wtoregen,
									xcoord*16,
									0,
									zcoord*16
								)
							);
							final Vector bvmax = BukkitUtil.toVector(
								new Location(
									wtoregen,
									xcoord*16+15,
									wtoregen.getMaxHeight(),
									zcoord*16+15
								)
							);
							//save
							plugin.debug("Saving Towny town "+town.getName()+" townblock to schematic");
							wrthread.getSchematicOperations().saveToSchematic(plugin.constants.getTownyTempFolder()+town.getName()+File.separator+"X"+xcoord+"Z"+zcoord, wtoregen, bvmin, bvmax);
							plugin.debug(town.getName()+" townblock saved");
						}
					}
					plugin.debug("Towmy claim "+town.getName()+" saved");
				}
			}
		}
		catch (NotRegisteredException e)
		{
			e.printStackTrace();
		}
	}

}
