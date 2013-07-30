package autosaveworld.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.entity.Player;

public class PermissionCheck {

	public static boolean isAllowed(CommandSender sender,final String commandName, String[] args)
	{
		boolean allowed = false;
		
		if ((sender instanceof Player)) {
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
				allowed = true;
			}
		} else if (sender instanceof ConsoleCommandSender || sender instanceof RemoteConsoleCommandSender) {
			// Success, this was from the Console or Remote Console
			allowed = true;
		}
		
		return allowed;
	}
	
}
