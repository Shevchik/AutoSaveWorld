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
import org.bukkit.Bukkit;
import org.bukkit.World;

import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColls;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.schematic.SchematicFormat;
import autosaveworld.core.AutoSaveWorld;

public class FactionsPaste {

	private AutoSaveWorld plugin;
	private World wtopaste;
	
	public FactionsPaste(AutoSaveWorld plugin, String worldtopasteto)
	{
		this.plugin = plugin;
		this.wtopaste = Bukkit.getWorld(worldtopasteto);
	}
	
	private int taskid;
	
	public void pasteAllFromSchematics()
	{
		plugin.debug("Pasting factions lands from schematics");
	    final SchematicFormat format = SchematicFormat.getFormats().iterator().next();
		final String schemfolder = "plugins/AutoSaveWorld/WorldRegenTemp/Factions/";
		
		for (final Faction f : FactionColls.get().getForWorld(wtopaste.getName()).getAll())
		{
		    	Runnable copypaste = new Runnable() {
					public void run(){
						try {
						plugin.debug("Restoring Faction land "+f.getName()+" to schematic");
						//copy to clipboard
						EditSession es = new EditSession(new BukkitWorld(wtopaste),Integer.MAX_VALUE);
						File file = new File(schemfolder+f.getName());
						CuboidClipboard cc = format.load(file);
						cc.place(es, cc.getOrigin(), false);
						plugin.debug("Pasted faction land "+f.getName()+" from schematics");
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
