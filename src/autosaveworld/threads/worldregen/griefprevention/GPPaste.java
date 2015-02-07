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

import java.util.Arrays;
import java.util.LinkedList;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

import org.bukkit.Bukkit;
import org.bukkit.World;

import autosaveworld.core.GlobalConstants;
import autosaveworld.core.logging.MessageLogger;
import autosaveworld.threads.worldregen.SchematicData.SchematicToLoad;
import autosaveworld.threads.worldregen.SchematicOperations;

public class GPPaste {

	private World wtopaste;

	public GPPaste(String worldtopasteto) {
		wtopaste = Bukkit.getWorld(worldtopasteto);
	}

	public void pasteAllFromSchematics() {
		MessageLogger.debug("Pasting GP regions from schematics");

		final String schemfolder = GlobalConstants.getGPTempFolder();

		GriefPrevention gp = (GriefPrevention) Bukkit.getPluginManager().getPlugin("GriefPrevention");

		// paste all claims
		for (Claim claim : gp.dataStore.getClaims()) {
			// paste
			MessageLogger.debug("Pasting GP region " + claim.getID() + " from schematics");
			SchematicToLoad schematicdata = new SchematicToLoad(schemfolder + claim.getID());
			SchematicOperations.pasteFromSchematic(wtopaste, new LinkedList<SchematicToLoad>(Arrays.asList(schematicdata)));
			MessageLogger.debug("Pasted GP region " + claim.getID() + " from schematics");
		}
	}

}
