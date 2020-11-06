/**
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 */

package autosaveworld.commands;

import java.util.Arrays;
import java.util.HashMap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import autosaveworld.commands.subcommands.BackupSubCommand;
import autosaveworld.commands.subcommands.ForceGCSubCommand;
import autosaveworld.commands.subcommands.ForceRestartSubCommand;
import autosaveworld.commands.subcommands.HelpSubCommand;
import autosaveworld.commands.subcommands.LocaleSubCommand;
import autosaveworld.commands.subcommands.PluginManagerSubCommand;
import autosaveworld.commands.subcommands.ProcessManagerSubCommand;
import autosaveworld.commands.subcommands.PurgeSubCommand;
import autosaveworld.commands.subcommands.ReloadAllSubCommand;
import autosaveworld.commands.subcommands.ReloadConfigMSGSubCommand;
import autosaveworld.commands.subcommands.ReloadConfigSubCommand;
import autosaveworld.commands.subcommands.RestartSubCommand;
import autosaveworld.commands.subcommands.SaveSubCommand;
import autosaveworld.commands.subcommands.ServerStatusSubCommand;
import autosaveworld.commands.subcommands.StopCommand;
import autosaveworld.commands.subcommands.VersionSubCommand;
import autosaveworld.commands.subcommands.WorldRegenSubCommand;
import autosaveworld.config.LocaleChanger;
import autosaveworld.core.AutoSaveWorld;
import autosaveworld.core.logging.MessageLogger;

public class NoTabCompleteCommandsHandler implements CommandExecutor {

	protected final LocaleChanger localeChanger = new LocaleChanger();

	protected final HashMap<String, ISubCommand> subcommandhandlers = new HashMap<>();

	public void initSubCommandHandlers() {
		subcommandhandlers.clear();
		subcommandhandlers.put("version", new VersionSubCommand());
		subcommandhandlers.put("help", new HelpSubCommand());
		subcommandhandlers.put("reload", new ReloadAllSubCommand());
		subcommandhandlers.put("reloadconfig", new ReloadConfigSubCommand());
		subcommandhandlers.put("reloadmsg", new ReloadConfigMSGSubCommand());
		subcommandhandlers.put("locale", new LocaleSubCommand(localeChanger));
		subcommandhandlers.put("process", new ProcessManagerSubCommand());
		subcommandhandlers.put("pmanager", new PluginManagerSubCommand());
		subcommandhandlers.put("forcegc", new ForceGCSubCommand());
		subcommandhandlers.put("serverstatus", new ServerStatusSubCommand());
		subcommandhandlers.put("save", new SaveSubCommand());
		subcommandhandlers.put("backup", new BackupSubCommand());
		subcommandhandlers.put("purge", new PurgeSubCommand());
		subcommandhandlers.put("restart", new RestartSubCommand());
		subcommandhandlers.put("forcerestart", new ForceRestartSubCommand());
		subcommandhandlers.put("regenworld", new WorldRegenSubCommand());
		subcommandhandlers.put("stop", new StopCommand());
	}

	protected PermissionCheck permCheck = new PermissionCheck();

	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {

		String commandName = command.getName().toLowerCase();

		// check permissions
		if (!permCheck.isAllowed(sender, commandName, args, AutoSaveWorld.getInstance().getMainConfig().commandOnlyFromConsole)) {
			MessageLogger.sendMessage(sender, AutoSaveWorld.getInstance().getMessageConfig().messageInsufficientPermissions);
			return true;
		}

		// now handle commands
		if (commandName.equalsIgnoreCase("autosave")) {
			// "autosave" command handler
			AutoSaveWorld.getInstance().getSaveThread().triggerTaskRun();
			return true;
		} else if (commandName.equalsIgnoreCase("autobackup")) {
			// "autobackup" command handler
			AutoSaveWorld.getInstance().getBackupThread().triggerTaskRun();
			return true;
		} else if (commandName.equalsIgnoreCase("autopurge")) {
			// "autopurge" command handler
			AutoSaveWorld.getInstance().getPurgeThread().triggerTaskRun();
			return true;
		} else if (commandName.equalsIgnoreCase("autosaveworld")) {
			// "autosaveworld" command handler
			if (args.length == 0) {
				return false;
			}
			String subcommand = args[0].toLowerCase();
			if (subcommandhandlers.containsKey(subcommand)) {
				ISubCommand handler = subcommandhandlers.get(subcommand);
				if (handler.getMinArguments() > (args.length - 1)) {
					MessageLogger.sendMessage(sender, "&4Not enough args");
					return true;
				} else {
					handler.handle(sender, Arrays.copyOfRange(args, 1, args.length));
					return true;
				}
			}
		}
		return false;
	}

}
