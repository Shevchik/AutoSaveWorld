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

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.RemoteServerCommandEvent;
import org.bukkit.event.server.ServerCommandEvent;

	public class ASWEventListener implements Listener, CommandExecutor {

		private AutoSave plugin = null;
		private AutoSaveConfig config;
		private AutoSaveConfigMSG configmsg;
		private LocaleContainer localeloader;
		ASWEventListener(AutoSave plugin, AutoSaveConfig config, AutoSaveConfigMSG configmsg, LocaleContainer localeloader){
			this.plugin = plugin;
			this.config = config;
			this.configmsg  = configmsg;
			this.localeloader = localeloader;
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
		
		
		@EventHandler
		public void onConsoleStopCommand(ServerCommandEvent event)
		{
		if (event.getCommand().equalsIgnoreCase("stop")||event.getCommand().equalsIgnoreCase("restart")||event.getCommand().equalsIgnoreCase("reload")) {
			{plugin.crashrestartThread.stopthread();} }
		}
		
		@EventHandler
		public void onRemoteConsoleStopCommand(RemoteServerCommandEvent event)
		{
		if (event.getCommand().equalsIgnoreCase("stop")||event.getCommand().equalsIgnoreCase("restart")||event.getCommand().equalsIgnoreCase("reload")) {
			{plugin.crashrestartThread.stopthread();} }
		}
		
		@Override
		public boolean onCommand(CommandSender sender, Command command,
		String commandLabel, String[] args) {
		String commandName = command.getName().toLowerCase();
		Player player = null;
		if ((sender instanceof Player)) {
		// Player, lets check if player isOp
		player = (Player) sender;

		String perm = null;
		if (commandName.equalsIgnoreCase("autosaveworld")) { if (args.length == 0) {perm="autosaveworld.autosaveworld";} else {perm="autosaveworld."+args[0];}
		} else 
		if (commandName.equalsIgnoreCase("autosave"))
		{perm = "autosaveworld.save";} else
		if (commandName.equalsIgnoreCase("autobackup"))
		{perm = "autosaveworld.backup";} else
		if (commandName.equalsIgnoreCase("autopurge"))
		{perm = "autosaveworld.purge";}
		// Check Permissions
		if (!player.isOp() && !player.hasPermission(perm)) 
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
		
		if (commandName.equalsIgnoreCase("autosaveworld")) {
		//help
		if (args.length==1 && args[0].equalsIgnoreCase("help"))
		{
		plugin.sendMessage(sender,"&f/asw help&7 - &3Shows this help");
		plugin.sendMessage(sender,"&f/asw save&7 - &3Saves all worlds");
		plugin.sendMessage(sender,"&f/save&7 - &3Same as /asw save");
		plugin.sendMessage(sender,"&f/asw backup&7 - &3Backups worlds defined in config.yml (* - all worlds)");
		plugin.sendMessage(sender,"&f/backup&7 - &3Same as /asw backup");
		plugin.sendMessage(sender,"&f/asw purge&7 - &3Purges plugins info from inactive players");
		plugin.sendMessage(sender,"&f/purge&7 - &3Same as /asw purge");
		plugin.sendMessage(sender,"&f/asw reload&7 - &3Reload all configs)");
		plugin.sendMessage(sender,"&f/asw reloadmsg&7 - &3Reload message config (configmsg.yml)");
		plugin.sendMessage(sender,"&f/asw reloadconfig&7 - &3Reload plugin config (config.yml)");
		plugin.sendMessage(sender,"&f/asw version&7 - &3Shows plugin version");
		plugin.sendMessage(sender,"&f/asw info&7 - &3Shows some info");
		plugin.sendMessage(sender,"&f/asw selfrestart&7 - &3Restart AutoSaveWorld");
		plugin.sendMessage(sender,"&f/asw locale&7 - &3Show current messages locale");
		plugin.sendMessage(sender,"&f/asw locale availible&7 - &3Show availible messages locale");
		plugin.sendMessage(sender,"&f/asw locale load {locale}&7 - &3Set meesages locale to one of the availible locales");
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
		} else
		if ((args.length==1 && args[0].equalsIgnoreCase("selfrestart")))
		{
		plugin.sendMessage(sender, "&9Restarting AutoSaveWorld");
		plugin.selfrestartThread.restart();
		return true;
		} else
		if ((args.length==1 && args[0].equalsIgnoreCase("restart"))) 
		{
		if (config.autorestartBroadcast) {plugin.broadcast(configmsg.messageAutoRestart);}
		plugin.debug("Restarting");
		plugin.JVMsh.setpath(config.autorestartscriptpath);
		Runtime.getRuntime().addShutdownHook(plugin.JVMsh);
		plugin.getServer().dispatchCommand(Bukkit.getConsoleSender(), "stop");
		return true;
		} else
		if ((args.length>=1 && args[0].equalsIgnoreCase("locale"))) 
		{
			if (args.length==1)
			{
				plugin.sendMessage(sender, "Current locale is "+config.langfilesuffix);
				return true;
			}
			else if (args.length == 2 && args[1].equalsIgnoreCase("availible"))
			{
				plugin.sendMessage(sender, "Availible locales: "+localeloader.getAvailibleLocales());
				return true;
			}
			else if (args.length == 2 && args[1].equalsIgnoreCase("load"))
			{
				plugin.sendMessage(sender, "You should specify a locale to load (get availible locales using /asw locale availible command)");
				return true;
			} 
			else if (args.length == 3 && args[1].equalsIgnoreCase("load"))
			{
				if (localeloader.getAvailibleLocales().contains(args[2]))
				{plugin.sendMessage(sender, "Loading locale "+args[2]);
				localeloader.loadLocale(args[2]);
				return true;}
				else 
				{plugin.sendMessage(sender, "Locale "+args[2]+" is not availible"); return true;}
			}
		}
		return false;
		} else
		if (commandName.equalsIgnoreCase("autosave"))
		{
		plugin.saveThread.startsave();
		return true;
		} else
		if (commandName.equalsIgnoreCase("autobackup"))
		{
		plugin.backupThread6.startbackup();
		return true;
		} else
		if (commandName.equalsIgnoreCase("autopurge"))
		{
		plugin.purgeThread.startpurge();
		return true;
		}
		return false;
	}
}