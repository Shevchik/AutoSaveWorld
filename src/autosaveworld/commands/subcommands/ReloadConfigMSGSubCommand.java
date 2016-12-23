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
import autosaveworld.config.loader.ConfigLoader;
import autosaveworld.core.AutoSaveWorld;
import autosaveworld.core.logging.MessageLogger;

public class ReloadConfigMSGSubCommand implements ISubCommand {

	@Override
	public void handle(CommandSender sender, String[] args) {
		ConfigLoader.loadAndSave(AutoSaveWorld.getInstance().getMessageConfig());
		MessageLogger.sendMessage(sender, "Messages file reloaded");
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
