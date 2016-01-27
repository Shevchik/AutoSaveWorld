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

package autosaveworld.features.purge.plugins;

import java.io.File;

import org.bukkit.Bukkit;

import autosaveworld.config.AutoSaveWorldConfig;
import autosaveworld.core.logging.MessageLogger;
import autosaveworld.features.purge.ActivePlayersList;
import autosaveworld.features.purge.DataPurge;
import autosaveworld.utils.FileUtils;

public class DatfilePurge extends DataPurge {

	public DatfilePurge(AutoSaveWorldConfig config, ActivePlayersList activeplayerslist) {
		super(config, activeplayerslist);
	}

	public void doPurge() {
		MessageLogger.debug("Player .dat file purge started");

		File worldfolder = Bukkit.getWorlds().get(0).getWorldFolder();
		File playersdatfolder = FileUtils.buildFile(worldfolder, "playerdata");
		File playersstatsfolder = FileUtils.buildFile(worldfolder, "stats");
		for (File playerfile : FileUtils.safeListFiles(playersdatfolder)) {
			if (playerfile.getName().endsWith(".dat")) {
				String playeruuid = playerfile.getName().substring(0, playerfile.getName().length() - 4);
				if (!activeplayerslist.isActiveUUID(playeruuid)) {
					MessageLogger.debug(playeruuid + " is inactive. Removing dat file");
					playerfile.delete();
					FileUtils.buildFile(playersstatsfolder, playerfile.getName()).delete();
					incDeleted();
				}
			}
		}

		MessageLogger.debug("Player .dat purge finished, deleted " + getDeleted() + " player .dat files");
	}

}
