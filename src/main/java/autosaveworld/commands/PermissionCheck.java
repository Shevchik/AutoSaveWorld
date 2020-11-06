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

package autosaveworld.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.entity.Player;

public class PermissionCheck {

	public boolean isAllowed(CommandSender sender, final String commandName, String[] args, boolean onlyfromconsole) {
		if ((sender instanceof Player) && !onlyfromconsole) {
			Player player = (Player) sender;
			// construct permissions name
			String perm = null;
			if (commandName.equalsIgnoreCase("autosaveworld")) {
				if (args.length == 0) {
					perm = "autosaveworld.autosaveworld";
				} else {
					perm = "autosaveworld." + args[0];
				}
			} else if (commandName.equalsIgnoreCase("autosave")) {
				perm = "autosaveworld.save";
			} else if (commandName.equalsIgnoreCase("autobackup")) {
				perm = "autosaveworld.backup";
			} else if (commandName.equalsIgnoreCase("autopurge")) {
				perm = "autosaveworld.purge";
			}
			// Check Permissions
			if (player.isOp() || player.hasPermission(perm)) {
				return true;
			}
		} else if ((sender instanceof ConsoleCommandSender) || (sender instanceof RemoteConsoleCommandSender)) {
			// Success, this was from the Console or Remote Console
			return true;
		}

		return false;
	}

}
