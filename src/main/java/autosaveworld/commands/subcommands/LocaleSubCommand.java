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

package autosaveworld.commands.subcommands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.command.CommandSender;

import autosaveworld.commands.ISubCommand;
import autosaveworld.config.LocaleChanger;
import autosaveworld.core.logging.MessageLogger;

public class LocaleSubCommand implements ISubCommand {

	private LocaleChanger localeChanger;
	public LocaleSubCommand(LocaleChanger localeChanger) {
		this.localeChanger = localeChanger;
	}

	@Override
	public void handle(CommandSender sender, String[] args) {
		if ((args.length == 1) && args[0].equalsIgnoreCase("available")) {
			MessageLogger.sendMessage(sender, "Available locales: " + localeChanger.getAvailableLocales());
			return;
		} else if ((args.length == 1) && args[0].equalsIgnoreCase("load")) {
			MessageLogger.sendMessage(sender, "You should specify a locale to load (get available locales using /asw locale available command)");
			return;
		} else if ((args.length == 2) && args[0].equalsIgnoreCase("load")) {
			String locale = args[1];
			if (localeChanger.getAvailableLocales().contains(locale)) {
				MessageLogger.sendMessage(sender, "Loading locale " + locale);
				localeChanger.loadLocale(locale);
				MessageLogger.sendMessage(sender, "Loaded locale " + locale);
				return;
			} else {
				MessageLogger.sendMessage(sender, "Locale " + locale + " is not available");
				return;
			}
		}
	}

	private List<String> cmds = Arrays.asList(new String[] {"available", "load"});
	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		if (args.length == 1) {
			ArrayList<String> result = new ArrayList<String>();
			for (String command : cmds) {
				if (command.startsWith(args[0])) {
					result.add(command);
				}
			}
			return result;
		}
		if (args.length == 2 && args[0].equalsIgnoreCase("load")) {
			ArrayList<String> result = new ArrayList<String>();
			for (String locale : localeChanger.getAvailableLocales()) {
				if (locale.startsWith(args[1])) {
					result.add(locale);
				}
			}
			return result;
		}
		return Collections.emptyList();
	}

	@Override
	public int getMinArguments() {
		return 0;
	}

}
