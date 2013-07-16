/**
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * 
 */

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
