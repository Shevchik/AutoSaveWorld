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

import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.List;

import org.bukkit.command.CommandSender;

import autosaveworld.commands.ISubCommand;
import autosaveworld.core.logging.MessageLogger;

public class ForceGCSubCommand implements ISubCommand {

	@Override
	public void handle(CommandSender sender, String[] args) {
		List<String> arguments = ManagementFactory.getRuntimeMXBean().getInputArguments();
		if (arguments.contains("-XX:+DisableExplicitGC")) {
			MessageLogger.sendMessage(sender, "&4Your JVM is configured to ignore GC calls, can't force gc");
			return;
		}
		MessageLogger.sendMessage(sender, "&9Forcing GC");
		System.gc();
		System.gc();
		MessageLogger.sendMessage(sender, "&9GC finished");
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		return Collections.emptyList();
	}

	@Override
	public int getMinArguments() {
		return 0;
	}

}
