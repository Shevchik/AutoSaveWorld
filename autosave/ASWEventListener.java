/**
*
* Copyright 2012 Shevchik
* 
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
		return true;
		} else
		//command to save worlds
		if (args.length==1 && args[0].equalsIgnoreCase("save")) {
		plugin.saveThread.startsave();
		return true;
		} else
		//command to backup worlds
		if (args.length==1 && args[0].equalsIgnoreCase("backup")) {
			plugin.backupThread6
			.startbackup();
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
		plugin.sendMessage(sender,"Configurations reloaded");
		return true;
		}
		if (args.length==1 && args[0].equalsIgnoreCase("reloadconfig")) {
		config.load();
		plugin.sendMessage(sender,"Configuration reloaded");
		return true;
		}
		if (args.length==1 && args[0].equalsIgnoreCase("reload")) {
		configmsg.loadmsg();
		plugin.sendMessage(sender,"Messages reloaded");
		return true;
		}
		if (args.length==1 && args[0].equalsIgnoreCase("version")) {
		plugin.sendMessage(sender,plugin.getDescription().getName()+" "+plugin.getDescription().getVersion());
		return true;
		}
		
		}
		return false;
		}
		
	}
	

	
	
	
	
	
	
	
	
	
	
	/* this code was previously used to handle messages
	
	private String world;
	private String perm;
	public boolean hasRight(Player player, String perm, String world) {
	world = player.getWorld().toString();
	//bPermissions
	if (getServer().getPluginManager().getPlugin("bPermissions") != null) {
	if (ApiLayer.hasPermission(world, CalculableType.USER, player.toString(), perm)) {return true;};}
	//bukkitPermissions
	if (player.hasPermission(perm)) {return true;};
	//PermissionsEx
	if (getServer().getPluginManager().getPlugin("PermissionsEx") !=null) { 
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

	if (commandName.equals("autosave")) {if (args.length>=1) { perm="autosave."+args[0];} else {perm="autosave.save";};} else
	if (commandName.equals("autobackup")) {if (args.length>=1) { perm="autobackup."+args[0];} else {perm="autobackup.backup";}}
	// Check Permissions
	if (!player.isOp() && !hasRight(player, perm, world)) 
	{
	sendMessage(sender, configmsg.messageInsufficientPermissions);
	return true;
	}
	} else if (sender instanceof ConsoleCommandSender) {
	// Success, this was from the Console
	} else {
	// Unknown, ignore these people with a pretty message
	sendMessage(sender, configmsg.messageInsufficientPermissions);
	return true;
	}


	if (commandName.equals("autosave")) {
	if (args.length == 0) {
		performSave();
	return true;
	}
	else if (args.length == 1 && args[0].equalsIgnoreCase("easteregg")) {
	sendMessage(sender, "Maybe something will be here later... ");
	return true;
	}
	else if (args.length == 1 && args[0].equalsIgnoreCase("reloadmsg")){
	configmsg.loadmsg();
	sendMessage(sender,
	"&9Messages loaded");
	return true;
	}
	else if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
	// Shows help for allowed commands

	// /save
	sendMessage(sender, "&f/save&7 - &3Saves all players & worlds");

	sendMessage(sender, "&f/save loadconfig&7 - &3Loads config from file config.yml");

	// /save help
	sendMessage(sender, "&f/save help&7 - &3Displays this dialogue");

	// /save toggle
	sendMessage(sender,
	"&f/save toggle&7 - &3Toggles the AutoSave system");

	sendMessage(sender,
	"&f/save reloadmsg&7 - &3Load messages from file configmsg.yml");

	// /save status
	sendMessage(sender,
	"&f/save status&7 - &3Reports thread status and last run time");

	// /save interval
	sendMessage(sender,
	"&f/save interval&7 [value] - &3Sets & retrieves the save interval");

	// /save broadcast
	sendMessage(sender,
	"&f/save broadcast&7 [on|off] - &3Sets & retrieves the broadcast value");

	// /save warn
	sendMessage(sender,
	"&f/save warn&7 [value] - &3Sets & retrieves the warn time in seconds");

	// /save version
	sendMessage(sender,
	"&f/save version&7 - &3Prints the version of AutoSave");
	} else if (args.length == 1 && args[0].equalsIgnoreCase("loadconfig")){
	config.load();		
	} else if (args.length == 1 && args[0].equalsIgnoreCase("toggle")) {
	// Start thread
	if (saveThread == null) {
	sendMessage(sender, configmsg.messageStarting);
	return startThread(ThreadType.SAVE);
	} else { // Stop thread
	sendMessage(sender, configmsg.messageStopping);
	return stopThread(ThreadType.SAVE);
	}
	} else if (args.length == 1 && args[0].equalsIgnoreCase("status")) {
	// Get Thread Status
	if (saveThread == null) {
	sendMessage(sender, configmsg.messageStatusOff);
	} else {
	if (saveThread.isAlive()) {
	if (lastSave == null) {
	sendMessage(sender, configmsg.messageStatusNotRun);
	return true;
	} else {
	sendMessage(sender,
	configmsg.messageStatusSuccess.replaceAll(
	"\\$\\{DATE\\}",
	lastSave.toString()));
	return true;
	}
	}
	else {
	sendMessage(sender, configmsg.messageStatusFail);
	return true;
	}
	}
	} else if (args.length >= 1 && args[0].equalsIgnoreCase("interval")) {
	if (args.length == 1) {
	// Report interval!
	sendMessage(
	sender,
	configmsg.messageInfoLookup.replaceAll(
	"\\$\\{VARIABLE\\}", "Interval")
	.replaceAll("\\$\\{VALUE\\}",
	String.valueOf(config.varInterval)));
	return true;
	} else if (args.length == 2) {
	// Change interval!
	try {
	int newInterval = Integer.parseInt(args[1]);
	config.varInterval = newInterval;
	sendMessage(sender,
	configmsg.messageInfoChangeSuccess.replaceAll(
	"\\$\\{VARIABLE\\}", "Interval"));
	return true;
	} catch (NumberFormatException e) {
	sendMessage(sender, configmsg.messageInfoNaN);
	return false;
	}
	}
	} else if (args.length >= 1 && args[0].equalsIgnoreCase("warn")) {
	if (args.length == 1) {
	// Report interval!
	sendMessage(
	sender,
	configmsg.messageInfoListLookup.replaceAll(
	"\\$\\{VARIABLE\\}", "Warn").replaceAll(
	"\\$\\{VALUE\\}",
	Generic.join(", ", config.varWarnTimes)));
	return true;
	} else if (args.length == 2) {
	// Change interval!
	try {
	ArrayList<Integer> tmpWarn = new ArrayList<Integer>();
	for (String s : args[1].split(",")) {
	tmpWarn.add(Integer.parseInt(s));
	}
	config.varWarnTimes = tmpWarn;
	sendMessage(sender,
	configmsg.messageInfoChangeSuccess.replaceAll(
	"\\$\\{VARIABLE\\}", "Warn"));
	return true;
	} catch (NumberFormatException e) {
	sendMessage(sender, configmsg.messageInfoNaN);
	return false;
	}
	}
	} else if (args.length >= 1
	&& args[0].equalsIgnoreCase("broadcast")) {
	if (args.length == 1) {
	// Report broadcast status!
	sendMessage(
	sender,
	configmsg.messageInfoLookup
	.replaceAll("\\$\\{VARIABLE\\}",
	"Broadcast")
	.replaceAll(
	"\\$\\{VALUE\\}",
	String.valueOf(config.varBroadcast ? config.valueOn
	: config.valueOff)));
	return true;
	} else if (args.length == 2) {
	// Change broadcast status!
	boolean newSetting = false;
	if (args[1].equalsIgnoreCase(config.valueOn)) {
	newSetting = true;
	} else if (args[1].equalsIgnoreCase(config.valueOff)) {
	newSetting = false;
	} else {
	sendMessage(sender,
	configmsg.messageInfoInvalid.replaceAll(
	"\\$\\{VALIDSETTINGS\\}", String
	.format("%s, %s",
	config.valueOn,
	config.valueOff)));
	return false;
	}
	config.varBroadcast = newSetting;
	sendMessage(sender,
	configmsg.messageInfoChangeSuccess.replaceAll(
	"\\$\\{VARIABLE\\}", "AutoSave Broadcast"));
	return true;
	}
	} else if (args.length >= 1 && args[0].equalsIgnoreCase("debug")) {
	if (args.length == 1) {
	// Report debug status!
	sendMessage(
	sender,
	configmsg.messageInfoLookup
	.replaceAll("\\$\\{VARIABLE\\}", "Debug")
	.replaceAll(
	"\\$\\{VALUE\\}",
	String.valueOf(config.varDebug ? config.valueOn
	: config.valueOff)));
	return true;
	} else if (args.length == 2) {
	// Change debug status!
	boolean newSetting = false;
	if (args[1].equalsIgnoreCase(config.valueOn)) {
	newSetting = true;
	} else if (args[1].equalsIgnoreCase(config.valueOff)) {
	newSetting = false;
	} else {
	sendMessage(sender,
	configmsg.messageInfoInvalid.replaceAll(
	"\\$\\{VALIDSETTINGS\\}", String
	.format("%s, %s",
	config.valueOn,
	config.valueOff)));
	return false;
	}
	config.varDebug = newSetting;
	sendMessage(sender,
	configmsg.messageInfoChangeSuccess.replaceAll(
	"\\$\\{VARIABLE\\}", "Debug"));
	return true;
	}
	}
	else if (args.length == 2 && args[0].equalsIgnoreCase("addworld")) {
	config.varWorlds.add(args[1]);
	sendMessage(sender, configmsg.messageInfoChangeSuccess.replaceAll(
	"\\$\\{VARIABLE\\}", "Worlds"));
	return true;
	} else if (args.length == 2 && args[0].equalsIgnoreCase("remworld")) {
	config.varWorlds.remove(args[1]);
	sendMessage(sender, configmsg.messageInfoChangeSuccess.replaceAll(
	"\\$\\{VARIABLE\\}", "Worlds"));
	return true;
	} else if (args.length == 1 && args[0].equalsIgnoreCase("world")) {
	sendMessage(
	sender,
	configmsg.messageInfoListLookup.replaceAll(
	"\\$\\{VARIABLE\\}", "Worlds").replaceAll(
	"\\$\\{VALUE\\}",
	Generic.join(", ", config.varWorlds)));
	return true;
	} else if (args.length == 1 && args[0].equalsIgnoreCase("version")) {
	sendMessage(sender, String.format(
	"%s%s",
	ChatColor.BLUE,
	configmsg.messageVersion.replaceAll("\\$\\{VERSION\\}",
	getDescription().getVersion()).replaceAll(
	"\\$\\{UUID\\}", config.varUuid.toString())));
	return true;
	}
	}
	else if (commandName.equals("autobackup")) {
	if (args.length == 0) 
	{
	if (config.javanio) {backupThread7.performBackup();} 
	else {backupThread6.performBackup();}
	return true;
	} 
	else if (args.length >= 1 && args[0].equalsIgnoreCase("slowbackup")) {
	if (args.length == 1) {if (config.slowbackup) {sendMessage(sender,"Slowbackup is on"); return true;}
	else {sendMessage(sender,"Slowbackup is on");return true;}
	} else if (args.length == 2){ if (args[1].equalsIgnoreCase("On"))
	{config.slowbackup = true;
	sendMessage(sender,"Slowbackup is enabled");}
	else if (args[1].equalsIgnoreCase("Off"))
	{config.slowbackup = false;
	sendMessage(sender,"Slowbackup is disabled");}
	return true;}
	} else if (args.length >= 1 && args[0].equalsIgnoreCase("addextfolder")) {
	if (args.length == 1) {sendMessage(sender, "&9Please specify external folder path");} else {
	  config.extfolders.add(args[1]);
	  config.savebackupextfolderconfig();
	  sendMessage(sender, "&9Folder added");}
	  return true;
	} else if (args.length >= 1 && args[0].equalsIgnoreCase("enabled")) {
		if (args.length == 1) 
		{
		if (config.backupEnabled == true ){sendMessage(sender, "&9AutoBackup is enabled");}
		else {sendMessage(sender, "&9AutoBackup is disabled");};
		return true;
		}
		else if (args.length == 2) {
		if (args[1].equalsIgnoreCase("on")) 
		{
				config.backupEnabled = true;			
				sendMessage(sender, "&9AutoBackup started");
				return true;} else 
				if (args[1].equalsIgnoreCase("off")) {
				config.backupEnabled = false;
				sendMessage(sender, "&9AutoBackup stopped");
				return true;}
		}
	}else if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
		//List of commands for autobackup
		sendMessage(sender, "&f/backup&7 - &3Backup all worlds");
		sendMessage(sender, "&f/backup enabled {on/of}&7 - &3Show status of autobackup {enable or disable autobackup}");
		sendMessage(sender, "&f/backup maxnumberbackups {number, 0 for infinite}&7 - &3Shown maximum number of backups {set maximum nubmer of backups}");
		sendMessage(sender, "&f/backup interval {seconds}&7 - &3Show interval between backups {set interval between backups}");
		sendMessage(sender, "&f/backup broadcast {on/off}&7 - &3Show broadcast status {enabled or disable broadcast}");
		sendMessage(sender, "&f/backup backuptoextfolders {on/off}&7 - &3{Enable or disable backup to external folders (!!!do not forget to add paths to file backupextfoldersconfig.yml!!!)}");
		sendMessage(sender, "&f/backup addextfolder {absolute path}&7 - - &3Add folder path to backupextfoldersconfig.yml");
		sendMessage(sender, "&f/backup donotbackuptointfolder {on/off}&7 - &3 {Disable or enable backup to internal folder if backup to external folder is active(on - disable, off - enable)}");
		sendMessage(sender, "&f/backup backuppluginsfolder {on/off}&7 - &3 {Disable or enable backup of plugins folder}");
		sendMessage(sender, "&f/backup slowbackup {on/off}&7 - &3 {Enable or disable slowbackup}");
		return true;
	} else if (args.length >= 1 && args[0].equalsIgnoreCase("donotbackuptointfolder")) {
		if (args.length == 1) {
		if (config.donotbackuptointfld) {sendMessage(sender, "Backup to internal folder is disabled"); } else
		{sendMessage(sender, "Backup to internal folder is enabled"); }
		return true;
	} else if (args.length == 2) {
		// Change donotbackuptointfld status!
		boolean newSetting = false;
		if (args[1].equalsIgnoreCase(config.valueOn)) {
		newSetting = true;
		} else if (args[1].equalsIgnoreCase(config.valueOff)) {
		newSetting = false;
		} else {
			sendMessage(sender,
					configmsg.messageInfoInvalid.replaceAll(
							"\\$\\{VALIDSETTINGS\\}", String
							.format("%s, %s",
									config.valueOn,
									config.valueOff)));
			return false;
		}
		config.donotbackuptointfld = newSetting;
		sendMessage(sender,
			configmsg.messageInfoChangeSuccess.replaceAll(
			"\\$\\{VARIABLE\\}", "AutoBackup Broadcast"));
		return true;
		}
	} else if (args.length >= 1 && args[0].equalsIgnoreCase("backuppluginsfolder")) {
		if (args.length ==1) 
			{if (config.backuppluginsfolder) {sendMessage(sender, "Bakup plugins folder is enabled");
			}else {sendMessage(sender, "Bakup plugins folder is disabled");}
			return true;}
		else if (args.length==2) 
		{if (args[1].equalsIgnoreCase("On"))
			{config.backuppluginsfolder = true;
			sendMessage(sender, "Backup plugins folder set to enabled");}
		else if (args[1].equalsIgnoreCase("Off"))
			{config.backuppluginsfolder = false;
			sendMessage(sender, "Backup plugins folder set to disabled");}
		}
	}
	else if (args.length >= 1 && args[0].equalsIgnoreCase("broadcast")) {
	if (args.length == 1) {
		// Report broadcast status!
		sendMessage(
		sender,
		configmsg.messageInfoLookup
		.replaceAll("\\$\\{VARIABLE\\}",
		"Broadcast")
		.replaceAll(
		"\\$\\{VALUE\\}",
		String.valueOf(config.backupBroadcast ? config.valueOn
		: config.valueOff)));
		return true;
	} else if (args.length == 2) {
		// Change broadcast status!
		boolean newSetting = false;
		if (args[1].equalsIgnoreCase(config.valueOn)) {
		newSetting = true;
		} else if (args[1].equalsIgnoreCase(config.valueOff)) {
		newSetting = false;
		} else {
			sendMessage(sender,
					configmsg.messageInfoInvalid.replaceAll(
							"\\$\\{VALIDSETTINGS\\}", String
							.format("%s, %s",
									config.valueOn,
									config.valueOff)));
			return false;
		}
		config.backupBroadcast = newSetting;
		sendMessage(sender,
			configmsg.messageInfoChangeSuccess.replaceAll(
			"\\$\\{VARIABLE\\}", "AutoBackup Broadcast"));
		return true;
		}
	} else if (args.length >= 1 && args[0].equalsIgnoreCase("interval")) {
		if (args.length == 1) {sendMessage(sender,"&9Interval is " + String.valueOf(config.backupInterval));
		return true;}
		else if (args.length == 2) {int newInterval = Integer.parseInt(args[1]);
		config.backupInterval = newInterval;
		sendMessage(sender,
		configmsg.messageInfoChangeSuccess.replaceAll(
		"\\$\\{VARIABLE\\}", "Interval"));
		return true;}
	} else if (args.length >= 1 && args[0].equalsIgnoreCase("maxnumberofbackups")) {
		if (args.length == 1) {sendMessage(sender, "&9Maximum number of backups is "+config.MaxNumberOfBackups);
		return true;}
		else if (args.length == 2) {int newInterval = Integer.parseInt(args[1]);
		config.MaxNumberOfBackups = newInterval;
		sendMessage(sender,configmsg.messageInfoChangeSuccess.replaceAll("\\$\\{VARIABLE\\}", "Maximum number of backups"));
		return true;}
	} else if (args.length >= 1 && args[0].equalsIgnoreCase("backuptoextfolders")) {
		if (args.length == 1) {
			if (config.backuptoextfolders) {
			sendMessage(sender, "Backup to external folders is on");} else {
			sendMessage(sender, "Backup to external folders is off");}	
			return true;
		} else if (args.length == 2) {
			boolean newSetting = false;
			if (args[1].equalsIgnoreCase(config.valueOn)) {
			newSetting = true;
			} else if (args[1].equalsIgnoreCase(config.valueOff)) {
			newSetting = false;
			} else {
				sendMessage(sender,
						configmsg.messageInfoInvalid.replaceAll(
								"\\$\\{VALIDSETTINGS\\}", String
								.format("%s, %s",
										config.valueOn,
										config.valueOff)));
				return false;
			}
			config.backuptoextfolders = newSetting;
			sendMessage(sender,
				configmsg.messageInfoChangeSuccess.replaceAll(
				"\\$\\{VARIABLE\\}", "AutoBackup save to external folders"));
			return true;
		}
	}


	}
	else {
	sendMessage(sender, String.format(
	"Unknown command \"%s\" handled by %s", commandName,
	getDescription().getName()));
	}
	return false;
	}
*/
