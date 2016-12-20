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

import java.util.Collections;
import java.util.List;

import org.bukkit.command.CommandSender;

import autosaveworld.commands.ISubCommand;
import autosaveworld.core.logging.MessageLogger;

public class HelpSubCommand implements ISubCommand {

	@Override
	public void handle(CommandSender sender, String[] args) {
		MessageLogger.sendMessage(sender, "&f/asw save&7 - &3Runs a save");
		MessageLogger.sendMessage(sender, "&f/save&7 - &3Same as /asw save");
		MessageLogger.sendMessage(sender, "&f/asw backup&7 - &3Does a backup");
		MessageLogger.sendMessage(sender, "&f/backup&7 - &3Same as /asw backup");
		MessageLogger.sendMessage(sender, "&f/asw purge&7 - &3Purges plugins info from inactive players");
		MessageLogger.sendMessage(sender, "&f/purge&7 - &3Same as /asw purge");
		MessageLogger.sendMessage(sender, "&f/asw restart&7 - &3Restarts server");
		MessageLogger.sendMessage(sender, "&f/asw forcerestart&7 - &3Restarts server without countdown");
		MessageLogger.sendMessage(sender, "&f/asw regenworld {world} {worldregionsfolder}&7 - &3Regenerates world (world regions folder is the folder where .mca files for this world are located)");
		MessageLogger.sendMessage(sender, "&f/asw pmanager load {pluginname}&7 - &3Loads plugin {pluginname}");
		MessageLogger.sendMessage(sender, "&f/asw pmanager unload {pluginname}&7 - &3Unloads plugin {pluginname}");
		MessageLogger.sendMessage(sender, "&f/asw pmanager reload {pluginname}&7 - &3Unload plugin {pluginname} and then loads it");
		MessageLogger.sendMessage(sender, "&f/asw process start {processname} {command line}&7 - &3Starts process using {command line}");
		MessageLogger.sendMessage(sender, "&f/asw process stop {processname}&7 - &3Stops process");
		MessageLogger.sendMessage(sender, "&f/asw process output {processname}&7 - &3Prints latest process output from output and error streams");
		MessageLogger.sendMessage(sender, "&f/asw process input {processname} {input}&7 - &3Sends a line to process input stream");
		MessageLogger.sendMessage(sender, "&f/asw process list&7 - &3Shows registered processes");
		MessageLogger.sendMessage(sender, "&f/asw serverstatus&7 - &3Shows cpu, memory, HDD usage");
		MessageLogger.sendMessage(sender, "&f/asw stop&7 - &3Stops the server");
		MessageLogger.sendMessage(sender, "&f/asw forcegc&7 - &3Forces garbage collection");
		MessageLogger.sendMessage(sender, "&f/asw reload&7 - &3Reload all configs)");
		MessageLogger.sendMessage(sender, "&f/asw reloadconfig&7 - &3Reload plugin config (config.yml)");
		MessageLogger.sendMessage(sender, "&f/asw reloadmsg&7 - &3Reload message config (configmsg.yml)");
		MessageLogger.sendMessage(sender, "&f/asw locale available&7 - &3Show available messages locales");
		MessageLogger.sendMessage(sender, "&f/asw locale load {locale}&7 - &3Set meesages locale to one of the available locales");
		MessageLogger.sendMessage(sender, "&f/asw version&7 - &3Shows plugin version");
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
