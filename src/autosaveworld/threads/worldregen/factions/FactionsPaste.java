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
import org.bukkit.World;

import com.massivecraft.factions.entity.BoardColls;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColls;
import com.massivecraft.mcore.ps.PS;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.schematic.SchematicFormat;

import autosaveworld.core.AutoSaveWorld;
import autosaveworld.threads.worldregen.WorldRegenConstants;

public class FactionsPaste {

	private AutoSaveWorld plugin;
	private World wtopaste;
	
	public FactionsPaste(AutoSaveWorld plugin, String worldtopasteto)
	{
		this.plugin = plugin;
		this.wtopaste = Bukkit.getWorld(worldtopasteto);
	}
	
	private int taskid;
    final SchematicFormat format = SchematicFormat.getFormats().iterator().next();
	
	public void pasteAllFromSchematics()
	{
		plugin.debug("Pasting factions lands from schematics");
		
		//paste users lands
		final String schemfolder = WorldRegenConstants.getFactionsTempFolder();
		for (final Faction f : FactionColls.get().getForWorld(wtopaste.getName()).getAll())
		{
		  	Set<PS> chunks = BoardColls.get().getChunks(f);
			if (chunks.size() != 0)
			{
				pasteFactionLand(f,schemfolder);
			}
		}
		
	}
	
	
	private void pasteFactionLand(final Faction f, final String schemfolder)
	{
		Set<PS> chunks = BoardColls.get().getChunks(f);
		plugin.debug("Pasting faction land "+f.getName()+" from schematic");
		for (PS ps : chunks)
		{
			final int xcoord = ps.getChunkX();
		 	final int zcoord = ps.getChunkZ();
		   	Runnable copypaste = new Runnable() 
	    	{
				public void run()
				{
					try {
						plugin.debug("Pasting "+f.getName()+" chunk from schematic");
						//load from schematic to clipboard
						EditSession es = new EditSession(new BukkitWorld(wtopaste),Integer.MAX_VALUE);
						File file = new File(schemfolder+f.getName()+File.separator+"X"+xcoord+"Z"+zcoord);
						CuboidClipboard cc = format.load(file);
						//paste clipboard at origin
						cc.place(es, cc.getOrigin(), false);
						plugin.debug("Pasted "+f.getName()+" chunk from schematic");
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
		plugin.debug("Pasted faction land "+f.getName()+" from schematic");
 	}
	
}
