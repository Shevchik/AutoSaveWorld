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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import com.massivecraft.factions.entity.BoardColls;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColls;
import com.massivecraft.mcore.ps.PS;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.schematic.SchematicFormat;
import com.sk89q.worldguard.bukkit.BukkitUtil;

import autosaveworld.core.AutoSaveWorld;
import autosaveworld.threads.worldregen.WorldRegenConstants;

public class FactionsCopy {

	private AutoSaveWorld plugin;
	private World wtoregen;
	
	public FactionsCopy(AutoSaveWorld plugin, String worldtoregen)
	{
		this.plugin = plugin;
		this.wtoregen = Bukkit.getWorld(worldtoregen);
	}
	
	private int taskid;
	
	public void copyAllToSchematics()
	{
		plugin.debug("Saving factions homes to schematics");
	    final SchematicFormat format = SchematicFormat.getFormats().iterator().next();
		final String schemfolder = WorldRegenConstants.getFactionsTempFolder();
		new File(schemfolder).mkdirs();
		//save all factions buildings
		for (final Faction f : FactionColls.get().getForWorld(wtoregen.getName()).getAll())
		{
		  	Set<PS> chunks = BoardColls.get().getChunks(f);
		  	//check if faction has claimed land
		   	if (chunks.size() != 0 && !f.getName().equals("WarZone") && !f.getName().equals("SafeZone"))
		   	{
		   		//now we will have to iterate over all chunks and find put the bounds
		    	List<Integer> xcoords = new ArrayList<Integer>(); List<Integer> zcoords = new ArrayList<Integer>();
		    	for (PS ps : chunks)
		    	{
		    		xcoords.add(ps.asBukkitChunk().getX());
		    		zcoords.add(ps.asBukkitChunk().getZ());
		    		System.out.println(ps.asBukkitChunk().getX());
		    		System.out.println(ps.asBukkitChunk().getZ());
		    	}
		    	Collections.sort(xcoords);
		    	Collections.sort(zcoords);
		    	//minimal position
		    	final Vector bvmin = BukkitUtil.toVector(
		    			new Location(
		    					wtoregen,
		    					xcoords.get(0)*16,
		    					0,
		    					zcoords.get(0)*16
		    			)
		    	);
		    	final Vector bvmax = BukkitUtil.toVector(
		    			new Location(
		    					wtoregen,
		    					xcoords.get(xcoords.size()-1)*16+15,
		    					wtoregen.getMaxHeight(),
		    					zcoords.get(zcoords.size()-1)*16+15
		    			)
		    	);
		    	Runnable copypaste = new Runnable() {
					public void run(){
						try {
						plugin.debug("Saving Faction land "+f.getName()+" to schematic");
						//copy to clipboard
						EditSession es = new EditSession(new BukkitWorld(wtoregen),Integer.MAX_VALUE);
						Vector pos = bvmax;
						CuboidClipboard clipboard = new CuboidClipboard(
								bvmax.subtract(bvmin).add(new Vector(1, 1, 1)),
								bvmin, bvmin.subtract(pos)
						);
						clipboard.copy(es);
						//save to schematic
				        File schematic = new File(schemfolder + f.getName());
				        format.save(clipboard, schematic);
				        plugin.debug("Faction Land "+f.getName()+" saved");
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
						e.printStackTrace();
					}
				}
		    }
		}
	}
}
