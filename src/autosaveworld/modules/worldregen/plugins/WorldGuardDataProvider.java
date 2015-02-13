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

package autosaveworld.modules.worldregen.plugins;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.World;

import autosaveworld.core.GlobalConstants;
import autosaveworld.modules.worldregen.SchematicData.SchematicToLoad;
import autosaveworld.modules.worldregen.SchematicData.SchematicToSave;
import autosaveworld.modules.worldregen.tasks.CopyDataProvider;
import autosaveworld.modules.worldregen.tasks.PasteDataProvider;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class WorldGuardDataProvider implements CopyDataProvider, PasteDataProvider {

	private World wtoregen;

	public WorldGuardDataProvider(World wtoregen) {
		this.wtoregen = wtoregen;
	}

	@Override
	public List<SchematicToSave> getSchematicsToCopy() {
		ArrayList<SchematicToSave> schematics = new ArrayList<SchematicToSave>();
		WorldGuardPlugin wg = (WorldGuardPlugin) Bukkit.getPluginManager().getPlugin("WorldGuard");
		final RegionManager m = wg.getRegionManager(wtoregen);
		for (final ProtectedRegion rg : m.getRegions().values()) {
			// ignore global region
			if (rg.getId().equalsIgnoreCase("__global__")) {
				continue;
			}
			String name = rg.getId();
			schematics.add(new SchematicToSave(
				GlobalConstants.getWGTempFolder() + name,
				rg.getMinimumPoint(),
				rg.getMaximumPoint(),
				"Saving WG Region " + name + " to schematic",
				"WG Region " + name + " saved")
			);
		}
		return schematics;
	}

	@Override
	public List<SchematicToLoad> getSchematicsToPaste() {
		ArrayList<SchematicToLoad> schematics = new ArrayList<SchematicToLoad>();
		WorldGuardPlugin wg = (WorldGuardPlugin) Bukkit.getPluginManager().getPlugin("WorldGuard");
		final RegionManager m = wg.getRegionManager(wtoregen);
		for (final ProtectedRegion rg : m.getRegions().values()) {
			// ignore global region
			if (rg.getId().equalsIgnoreCase("__global__")) {
				continue;
			}
			String name = rg.getId();
			schematics.add(new SchematicToLoad(
				GlobalConstants.getWGTempFolder() + name,
				"Pasting WG Region " + name + " from schematic",
				"WG Region " + name + " pasted")
			);
		}
		return schematics;
	}

}