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

package autosave;



import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;
import de.bananaco.bpermissions.api.ApiLayer;
import de.bananaco.bpermissions.api.util.CalculableType;
	
	public class ASWEventListener implements Listener, CommandExecutor {

		private AutoSave plugin = null;
		private AutoSaveConfig config;
		private AutoSaveConfigMSG configmsg;
		ASWEventListener(AutoSave plugin, AutoSaveConfig config, AutoSaveConfigMSG configmsg){
			this.plugin = plugin;
			this.config = config;
			this.configmsg  = configmsg;
		};
		
		
		
		@EventHandler
		public void onPlayerQuit(PlayerQuitEvent event) {
		if (config.varDebug) {
		plugin.debug("Check for last leave");
		plugin.debug("Players online = "+(plugin.getServer().getOnlinePlayers().length-1));}
		if (plugin.getServer().getOnlinePlayers().length == 1) {
		plugin.debug("Last player has quit, autosaving");
		plugin.saveThread.startsave();}
		}
		
		private String world;
		private String perm;
		private boolean hasRight(Player player, String perm, String world) {
		world = player.getWorld().toString();
		//bPermissions
		if (plugin.getServer().getPluginManager().getPlugin("bPermissions") != null) {
		if (ApiLayer.hasPermission(world, CalculableType.USER, player.toString(), perm)) {return true;};}
		//bukkitPermissions
		if (player.hasPermission(perm)) {return true;};
		//PermissionsEx
		if (plugin.getServer().getPluginManager().getPlugin("PermissionsEx") !=null) { 
			PermissionUser user = PermissionsEx.getUser(player);
			if (user.has(perm)) {return true;} ;}
		return false;
		}
		
		@Override
		public boolean onCommand(CommandSender sender, Command command,
		String commandLabel, String[] args) {
		String commandName = command.getName().toLowerCase();
		Player player = null;
		if ((sender instanceof Player)) {
		// Player, lets check if player isOp()
		player = (Player) sender;

		if (commandName.equals("autosaveworld")){ if (args.length == 0) {perm="autosaveworld.autosaveworld";} else {perm="autosaveworld."+args[0];}}
		
		// Check Permissions
		if (!player.isOp() && !hasRight(player, perm, world)) 
		{
		plugin.sendMessage(sender, configmsg.messageInsufficientPermissions);
		return true;
		}
		} else if (sender instanceof ConsoleCommandSender || sender instanceof RemoteConsoleCommandSender) {
		// Success, this was from the Console or Remote Console
		} else {
		// Who are you people?
		plugin.sendMessage(sender, configmsg.messageInsufficientPermissions);
		return true;
		}
		if (commandName.equals("autosaveworld")) {
		//help
		if (args.length==1 && args[0].equalsIgnoreCase("help"))
		{
		plugin.sendMessage(sender,"&f/asw help&7 - &3Shows this help");
		plugin.sendMessage(sender,"&f/asw save&7 - &3Saves all worlds");
		plugin.sendMessage(sender,"&f/asw backup&7 - &3Backups worlds defined in config.yml (* - all worlds)");
		plugin.sendMessage(sender,"&f/asw purge&7 - &3Purges plugins info from inactive players");
		plugin.sendMessage(sender,"&f/asw reload&7 - &3Reload all configs)");
		plugin.sendMessage(sender,"&f/asw reloadmsg&7 - &3Reload message config (configmsg.yml)");
		plugin.sendMessage(sender,"&f/asw reloadconfig&7 - &3Reload plugin config (config.yml)");
		plugin.sendMessage(sender,"&f/asw version&7 - &3Shows plugin version");
		plugin.sendMessage(sender,"&f/asw info&7 - &3Shows some info");
		return true;
		} else
		//command to save worlds
		if (args.length==1 && args[0].equalsIgnoreCase("save")) {
		plugin.saveThread.startsave();
		return true;
		} else
		//command to backup worlds
		if (args.length==1 && args[0].equalsIgnoreCase("backup")) {
			plugin.backupThread6.startbackup();
		return true;
		} else
		//purge command
		if (args.length==1 && args[0].equalsIgnoreCase("purge")) {
		plugin.purgeThread.startpurge();
		return true;
		} else
		//reload command
		if (args.length==1 && args[0].equalsIgnoreCase("reload")) {
		config.load();
		configmsg.loadmsg();
		plugin.sendMessage(sender,"[AutoSaveWorld] all configurations reloaded");
		return true;
		} else
		if (args.length==1 && args[0].equalsIgnoreCase("reloadconfig")) {
		config.load();
		plugin.sendMessage(sender,"[AutoSaveWorld] main configuration reloaded");
		return true;
		} else
		if (args.length==1 && args[0].equalsIgnoreCase("reload")) {
		configmsg.loadmsg();
		plugin.sendMessage(sender,"[AutoSaveWorld] messages reloaded");
		return true;
		} else
		if (args.length==1 && args[0].equalsIgnoreCase("version")) {
		plugin.sendMessage(sender,plugin.getDescription().getName()+" "+plugin.getDescription().getVersion());
		return true;
		} else
		if (args.length==1 && args[0].equalsIgnoreCase("info")) {
			plugin.sendMessage(sender,"&9======AutoSaveWorld Info & Status======");
			if (plugin.saveThread!=null && plugin.saveThread.isAlive()) {
			plugin.sendMessage(sender,"&2AutoSave is active");
			plugin.sendMessage(sender,"&2Last save time: "+plugin.LastSave);}
			else {plugin.sendMessage(sender,"&4AutoSave is dead, i don't know how it happened, but it's dead, jim");}
			if (config.backupEnabled) {
				if (plugin.backupThread6!=null && plugin.backupThread6.isAlive()) {
				plugin.sendMessage(sender,"&2AutoBackup is active");
				plugin.sendMessage(sender,"&2Last backup time: "+plugin.LastSave);}
				else {plugin.sendMessage(sender,"&4AutoBackup is dead, i don't know how it happened, but it's dead, jim"); }
				}
			else {
				if (plugin.backupThread6!=null && plugin.backupThread6.isAlive()) {
				plugin.sendMessage(sender,"&2AutoBackup is inactive"); }
				else {plugin.sendMessage(sender,"&4AutoBackup is dead, i don't know how it happened, but it's dead, jim");}
				}
			plugin.sendMessage(sender,"&9====================================");
			return true;
			}
		}
		return false;
		}
	}