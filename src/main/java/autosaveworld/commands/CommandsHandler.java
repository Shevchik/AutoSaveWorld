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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import autosaveworld.core.AutoSaveWorld;

public class CommandsHandler extends NoTabCompleteCommandsHandler implements TabCompleter {

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String commandLabel, String[] args) {
		ArrayList<String> result = new ArrayList<>();
		if (command.getName().equals("autosaveworld")) {
			if (args.length == 1) {
				for (String subcommandname : subcommandhandlers.keySet()) {
					if (
						subcommandname.startsWith(args[0].toLowerCase()) &&
						permCheck.isAllowed(sender, command.getName(), new String[] {subcommandname}, AutoSaveWorld.getInstance().getMainConfig().commandOnlyFromConsole)
					) {
						result.add(subcommandname);
					}
				}
				return result;
			} else {
				String subcommandname = args[0].toLowerCase();
				if (!permCheck.isAllowed(sender, command.getName(), args, AutoSaveWorld.getInstance().getMainConfig().commandOnlyFromConsole)) {
					return Collections.emptyList();
				}
				if (subcommandhandlers.containsKey(subcommandname)) {
					return subcommandhandlers.get(subcommandname).tabComplete(sender, Arrays.copyOfRange(args, 1, args.length));
				}
			}
		}
		return new ArrayList<>();
	}

}
