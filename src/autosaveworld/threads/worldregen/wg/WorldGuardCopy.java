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

package autosaveworld.threads.worldregen.wg;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;

import org.bukkit.Bukkit;
import org.bukkit.World;

import autosaveworld.core.GlobalConstants;
import autosaveworld.core.logging.MessageLogger;
import autosaveworld.threads.worldregen.SchematicData.SchematicToSave;
import autosaveworld.threads.worldregen.SchematicOperations;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class WorldGuardCopy {

	private World wtoregen;
	public WorldGuardCopy(String worldtoregen) {
		this.wtoregen = Bukkit.getWorld(worldtoregen);
	}

	public void copyAllToSchematics() {
		MessageLogger.debug("Saving WG regions to schematics");

		WorldGuardPlugin wg = (WorldGuardPlugin) Bukkit.getPluginManager().getPlugin("WorldGuard");

		new File(GlobalConstants.getWGTempFolder()).mkdirs();

		final RegionManager m = wg.getRegionManager(wtoregen);
		for (final ProtectedRegion rg : m.getRegions().values()) {
			//ignore global region
			if (rg.getId().equalsIgnoreCase("__global__")) {continue;}
			//save
			MessageLogger.debug("Saving WG Region "+rg.getId()+" to schematic");
			SchematicToSave schematicdata = new SchematicToSave(GlobalConstants.getWGTempFolder()+rg.getId(), wtoregen, rg.getMinimumPoint(), rg.getMaximumPoint());
			SchematicOperations.saveToSchematic(new LinkedList<SchematicToSave>(Arrays.asList(schematicdata)));
			MessageLogger.debug("WG Region "+rg.getId()+" saved");
		}
	}

}