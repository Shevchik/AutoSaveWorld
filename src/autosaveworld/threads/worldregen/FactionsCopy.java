package autosaveworld.threads.worldregen;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;

import com.massivecraft.factions.entity.BoardColls;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColls;
import com.massivecraft.mcore.ps.PS;

import autosaveworld.core.AutoSaveWorld;

public class FactionsCopy {

	private AutoSaveWorld plugin;
	private World wtoregen;
	
	public FactionsCopy(AutoSaveWorld plugin, String worldtoregen)
	{
		this.plugin = plugin;
		this.wtoregen = Bukkit.getWorld(worldtoregen);
	}
	
	
	public void copyAllToSchematics()
	{
		plugin.debug("Saving factions homes to schematics");
		//get factions
		for (Faction f : FactionColls.get().getForWorld(wtoregen.getName()).getAll())
		{
		  	Set<PS> chunks = BoardColls.get().getChunks(f);
		  	//check if faction has claimed land
		   	if (chunks.size() != 0)
		   	{
		   		//now we will have to iterate over all chunks and find put the bounds
		    	Chunk cfirst = chunks.iterator().next().asBukkitChunk();
		    	int xmin = cfirst.getX();int zmin = cfirst.getZ();int xmax=cfirst.getX()+15; int zmax = cfirst.getZ()+15;
		    	for (PS ps : chunks)
		    	{
		    		System.out.println(ps.asBukkitChunk().getX());
		    		System.out.println(ps.asBukkitChunk().getZ());
		    		ps.asBukkitChunk();
		    	}
		    }
		}
	}
}
