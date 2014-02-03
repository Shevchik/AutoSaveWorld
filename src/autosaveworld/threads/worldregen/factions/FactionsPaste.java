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

import autosaveworld.core.AutoSaveWorld;
import autosaveworld.threads.worldregen.WorldRegenPasteThread;

public class FactionsPaste {

	private AutoSaveWorld plugin;
	private WorldRegenPasteThread wrthread;
	private World wtopaste;
	public FactionsPaste(AutoSaveWorld plugin, WorldRegenPasteThread wrthread, String worldtopasteto)
	{
		this.plugin = plugin;
		this.wrthread = wrthread;
		this.wtopaste = Bukkit.getWorld(worldtopasteto);
	}

	public void pasteAllFromSchematics()
	{
		plugin.debug("Pasting factions lands from schematics");

		String schemfolder = plugin.constants.getFactionsTempFolder();

		for (final Faction f : FactionColls.get().getForWorld(wtopaste.getName()).getAll())
		{
			plugin.debug("Pasting faction land "+f.getName()+" from schematic");
		  	Set<PS> chunks = BoardColls.get().getChunks(f);
			if (chunks.size() != 0)
			{
				//paste all chunks
				for (PS ps: chunks)
				{
					final int xcoord = ps.getChunkX();
				 	final int zcoord = ps.getChunkZ();
				 	//paste
					plugin.debug("Pasting "+f.getName()+" chunk from schematic");
					wrthread.getSchematicOperations().pasteFromSchematics(schemfolder+f.getName()+File.separator+"X"+xcoord+"Z"+zcoord, wtopaste);
					plugin.debug("Pasted "+f.getName()+" chunk from schematic");
				}
			}
			plugin.debug("Pasted faction land "+f.getName()+" from schematic");
		}
	}

}
