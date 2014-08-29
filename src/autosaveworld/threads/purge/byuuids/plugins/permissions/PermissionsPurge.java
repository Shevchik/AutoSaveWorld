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

package autosaveworld.threads.purge.byuuids.plugins.permissions;

import org.bukkit.Bukkit;

import autosaveworld.core.logging.MessageLogger;
import autosaveworld.threads.purge.byuuids.ActivePlayersList;

public class PermissionsPurge {

	public void doPermissionsPurgeTask(ActivePlayersList activePlayersStorage) {
		if (Bukkit.getPluginManager().getPlugin("GroupManager") != null) {
			MessageLogger.debug("GroupManager found, purging");
			new GroupManagerPurge().doPurge(activePlayersStorage);
			return;
		}
		if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
			MessageLogger.debug("Vault found, purging permissions");
			new VaultPurge().doPurge(activePlayersStorage);
		}
	}

}
