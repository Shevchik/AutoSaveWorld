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

package autosaveworld.threads.worldregen.factions;

import java.io.File;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import com.massivecraft.factions.entity.BoardColls;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColls;
import com.massivecraft.mcore.ps.PS;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.bukkit.BukkitUtil;

import autosaveworld.core.AutoSaveWorld;
import autosaveworld.threads.worldregen.WorldRegenCopyThread;

public class FactionsCopy {

	private AutoSaveWorld plugin;
	private WorldRegenCopyThread wrthread;
	private World wtoregen;
	public FactionsCopy(AutoSaveWorld plugin, WorldRegenCopyThread wrthread, String worldtoregen)
	{
		this.plugin = plugin;
		this.wtoregen = Bukkit.getWorld(worldtoregen);
		this.wrthread = wrthread;
	}
	
	public void copyAllToSchematics()
	{
		plugin.debug("Saving factions lands to schematics");

		new File(plugin.constants.getFactionsTempFolder()).mkdirs();
		
		for (final Faction f : FactionColls.get().getForWorld(wtoregen.getName()).getAll())
		{
		  	Set<PS> chunks = BoardColls.get().getChunks(f);
		   	if (chunks.size() != 0)
		   	{
				plugin.debug("Saving faction land "+f.getName()+" to schematic");
				for (PS ps : chunks)
				{
					plugin.debug("Saving "+f.getName()+" chunk to schematic");
					new File(plugin.constants.getFactionsTempFolder()+f.getName()).mkdirs();
					final int xcoord = ps.getChunkX();
				 	final int zcoord = ps.getChunkZ();
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
					wrthread.saveToSchematic(plugin.constants.getFactionsTempFolder()+f.getName()+File.separator, "X"+xcoord+"Z"+zcoord, wtoregen, bvmin, bvmax);
			        plugin.debug(f.getName()+" chunk saved");
				}
		        plugin.debug("faction land "+f.getName()+" saved");
		    }
		}
	}	

}
