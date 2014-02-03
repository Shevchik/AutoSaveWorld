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

package autosaveworld.threads.worldregen.griefprevention;

import java.lang.reflect.Field;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimArray;
import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

import org.bukkit.Bukkit;
import org.bukkit.World;

import autosaveworld.core.AutoSaveWorld;
import autosaveworld.threads.worldregen.WorldRegenPasteThread;

public class GPPaste {

	private AutoSaveWorld plugin;
	private WorldRegenPasteThread wrthread;
	private World wtopaste;
	public GPPaste(AutoSaveWorld plugin, WorldRegenPasteThread wrthread, String worldtopasteto)
	{
		this.plugin = plugin;
		this.wrthread = wrthread;
		this.wtopaste = Bukkit.getWorld(worldtopasteto);
	}



	public void pasteAllFromSchematics()
	{
		plugin.debug("Pasting GP regions from schematics");

		final String schemfolder = plugin.constants.getGPTempFolder();

		GriefPrevention gp = (GriefPrevention) Bukkit.getPluginManager().getPlugin("GriefPrevention");
		//get database
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

		//paste all claims
		for (int i = 0; i<ca.size(); i++)
		{
			Claim claim = ca.get(i);
			//paste
			plugin.debug("Pasting GP region "+claim.getID()+" from schematics");
			wrthread.getSchematicOperations().pasteFromSchematics(schemfolder+claim.getID(), wtopaste);
			plugin.debug("Pasted GP region "+claim.getID()+" from schematics");
		}
	}

}
