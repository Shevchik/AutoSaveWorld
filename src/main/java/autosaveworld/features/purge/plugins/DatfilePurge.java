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
import org.bukkit.OfflinePlayer;

import autosaveworld.core.logging.MessageLogger;
import autosaveworld.features.purge.ActivePlayersList;
import autosaveworld.features.purge.DataPurge;
import autosaveworld.utils.FileUtils;

public class DatfilePurge extends DataPurge {

	public DatfilePurge(ActivePlayersList activeplayerslist) {
		super("Player data files", activeplayerslist);
	}

	public void doPurge() {
		File worldfolder = Bukkit.getWorlds().get(0).getWorldFolder();
		File playersdatfolder = FileUtils.buildFile(worldfolder, "playerdata");
		File playersstatsfolder = FileUtils.buildFile(worldfolder, "stats");
		for (OfflinePlayer player : activeplayerslist.getAllPlayers()) {
			if (!activeplayerslist.isActiveUUID(player.getUniqueId())) {
				MessageLogger.debug(player.getUniqueId() + " is inactive. Removing dat file");
				FileUtils.buildFile(playersdatfolder, player.getUniqueId().toString() + ".dat").delete();
				FileUtils.buildFile(playersstatsfolder, player.getUniqueId().toString() + ".dat").delete();
				FileUtils.buildFile(playersstatsfolder, player.getUniqueId().toString() + ".json").delete();
				incDeleted();
			}
		}
	}

}
