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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import autosaveworld.commands.ISubCommand;
import autosaveworld.core.logging.MessageLogger;
import autosaveworld.features.worldregen.WorldRegenThread;

public class WorldRegenSubCommand implements ISubCommand {

	@Override
	public void handle(CommandSender sender, String[] args) {
		if (Bukkit.getPluginManager().getPlugin("WorldEdit") == null) {
			MessageLogger.sendMessage(sender, "You need WorldEdit installed to do that");
			return;
		}
		if (Bukkit.getWorld(args[0]) == null) {
			MessageLogger.sendMessage(sender, "This world doesn't exist");
			return;
		}
		if (!new File(args[1]).exists()) {
			MessageLogger.sendMessage(sender, "This folder doesn't exist");
		}
		new WorldRegenThread(args[0], args[1]).start();
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		if (args.length == 1) {
			ArrayList<String> result = new ArrayList<String>();
			for (World world : Bukkit.getWorlds()) {
				if (world.getName().startsWith(args[0])) {
					result.add(world.getName());
				}
			}
			return result;
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public int getMinArguments() {
		return 2;
	}

}
