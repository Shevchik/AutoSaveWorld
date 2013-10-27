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

package autosaveworld.commands;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import autosaveworld.config.AutoSaveConfig;
import autosaveworld.config.AutoSaveConfigMSG;
import autosaveworld.config.LocaleChanger;
import autosaveworld.core.AutoSaveWorld;

public class CommandsHandler implements CommandExecutor {

	private AutoSaveWorld plugin = null;
	private AutoSaveConfig config;
	private AutoSaveConfigMSG configmsg;
	private LocaleChanger localeChanger;
	public CommandsHandler(AutoSaveWorld plugin, AutoSaveConfig config,
			AutoSaveConfigMSG configmsg, LocaleChanger localeChanger) {
		this.plugin = plugin;
		this.config = config;
		this.configmsg = configmsg;
		this.localeChanger = localeChanger;
	};
	
	private PermissionCheck permCheck = new PermissionCheck();

	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		
		String commandName = command.getName().toLowerCase();

		//check permissions
		if (!permCheck.isAllowed(sender, commandName, args)) {
			plugin.sendMessage(sender, configmsg.messageInsufficientPermissions);
			return true;
		}
		
		// now handle commands
		if (commandName.equalsIgnoreCase("autosave")) {
			//"autosave" command handler
			plugin.saveThread.startsave();
			return true;
		} else if (commandName.equalsIgnoreCase("autobackup")) {
			//"autobackup" command handler
			plugin.backupThread6.startbackup();
			return true;
		} else if (commandName.equalsIgnoreCase("autopurge")) {
			//"autopurge" command handler
			plugin.purgeThread.startpurge();
			return true;
		} else if (commandName.equalsIgnoreCase("autosaveworld")) {
			//"autosaveworld" command handler
			if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
				// help
				plugin.sendMessage(sender, "&f/asw help&7 - &3Shows this help");
				plugin.sendMessage(sender, "&f/asw pmanager load {pluginname}&7 - &3Loads plugin {pluginname}");
				plugin.sendMessage(sender, "&f/asw pmanager unload {pluginname}&7 - &3Unloads plugin {pluginname}");
				plugin.sendMessage(sender, "&f/asw pmanager reload {pluginname}&7 - &3Reloads(unloads and then loads) plugin {pluginname}");
				plugin.sendMessage(sender, "&f/asw save&7 - &3Saves all worlds and players");
				plugin.sendMessage(sender, "&f/save&7 - &3Same as /asw save");
				plugin.sendMessage(sender, "&f/asw backup&7 - &3Backups worlds defined in config.yml (* - all worlds) and plugins (if enabled in config)");
				plugin.sendMessage(sender, "&f/backup&7 - &3Same as /asw backup");
				plugin.sendMessage(sender, "&f/asw purge&7 - &3Purges plugins info from inactive players");
				plugin.sendMessage(sender, "&f/purge&7 - &3Same as /asw purge");
				plugin.sendMessage(sender, "&f/asw restart&7 - &3Restarts server");
				plugin.sendMessage(sender, "&f/asw regenworld {world}&7 - &3Regenerates world");
				plugin.sendMessage(sender, "&f/asw reload&7 - &3Reload all configs)");
				plugin.sendMessage(sender, "&f/asw reloadconfig&7 - &3Reload plugin config (config.yml)");
				plugin.sendMessage(sender, "&f/asw reloadmsg&7 - &3Reload message config (configmsg.yml)");
				plugin.sendMessage(sender, "&f/asw locale&7 - &3Show current messages locale");
				plugin.sendMessage(sender, "&f/asw locale available&7 - &3Show available messages locales");
				plugin.sendMessage(sender, "&f/asw locale load {locale}&7 - &3Set meesages locale to one of the available locales");
				plugin.sendMessage(sender, "&f/asw info&7 - &3Shows some info");
				plugin.sendMessage(sender, "&f/asw version&7 - &3Shows plugin version");
				return true;
			} else if (args.length >= 3 && args[0].equalsIgnoreCase("pmanager")) {
				String[] nameArray = Arrays.copyOfRange(args, 2, args.length);
				StringBuilder sb = new StringBuilder(50);
				for (String namearg : nameArray)
				{
					sb.append(namearg);
					sb.append(" ");
				}
				sb.deleteCharAt(sb.length()-1);
				plugin.pmanager.handlePluginManagerCommand(sender, args[1], sb.toString());
				return true;
			} else if (args.length == 1 && args[0].equalsIgnoreCase("serverstatus")) {
				DecimalFormat df = new DecimalFormat("0.00");
				//hard drive
				File file = new File(".");
				long maxspacegb = file.getTotalSpace()/1024/1024/1024;
				long freespacegb = file.getFreeSpace()/1024/1024/1024;
				sender.sendMessage(ChatColor.GOLD+"Max disk space: "+ChatColor.GREEN+maxspacegb+"GB");
				sender.sendMessage(ChatColor.GOLD+"Free disk space: "+ChatColor.GREEN+freespacegb+"GB");
				sender.sendMessage(ChatColor.GOLD+"Disk usage: "+ChatColor.GREEN+df.format((maxspacegb-freespacegb)*100/maxspacegb)+"%");
				//memory
				Runtime runtime = Runtime.getRuntime();
				long maxmemmb = runtime.maxMemory()/1024/1024;
				long freememmb = (runtime.maxMemory()-(runtime.totalMemory()-runtime.freeMemory()))/1024/1024;
				sender.sendMessage(ChatColor.GOLD+"Max memory: "+ChatColor.GREEN+maxmemmb+"MB");
				sender.sendMessage(ChatColor.GOLD+"Free memory: "+ChatColor.GREEN+freememmb+"MB");
				sender.sendMessage(ChatColor.GOLD+"Memory usage: "+ChatColor.GREEN+df.format((maxmemmb-freememmb)*100/maxmemmb)+"%");
				//processor (if available)
				try {
					com.sun.management.OperatingSystemMXBean systemBean = (com.sun.management.OperatingSystemMXBean) java.lang.management.ManagementFactory.getOperatingSystemMXBean();
					double cpuusage = systemBean.getProcessCpuLoad()*100;
					if (cpuusage > 0) {
						sender.sendMessage(ChatColor.GOLD+"Cpu usage: "+ChatColor.GREEN+df.format(cpuusage)+"%");
					} else {
						sender.sendMessage(ChatColor.GOLD+"Cpu usage: "+ChatColor.RED+"not available");
					}
				} catch (Exception e) {}
				return true;
			}	else if (args.length == 1 && args[0].equalsIgnoreCase("save")) {
				//save
				plugin.saveThread.startsave();
				return true;
			} else if (args.length == 1 && args[0].equalsIgnoreCase("backup")) {
				//backup
				plugin.backupThread6.startbackup();
				return true;
			} else if (args.length == 1 && args[0].equalsIgnoreCase("purge")) {
				//purge
				plugin.purgeThread.startpurge();
				return true;
			} else if ((args.length == 1 && args[0].equalsIgnoreCase("restart"))) {
				//restart
				plugin.autorestartThread.startrestart(false);
				return true;
			} else if ((args.length == 2 && args[0].equalsIgnoreCase("regenworld"))) {
				//regen world
				if (Bukkit.getPluginManager().getPlugin("WorldEdit") == null) {
					plugin.sendMessage(sender, "[AutoSaveWorld] You need WorldEdit installed to do that");
					return true;
				}
				if (Bukkit.getWorld(args[1]) == null) {
					plugin.sendMessage(sender, "[AutoSaveWorld] This world doesn't exist");
					return true;
				}
				if (plugin.worldregenInProcess) {
					plugin.sendMessage(sender, "[AutoSaveWorld] Please wait before previous world regeneration is finished");
					return true;
				}
				plugin.worldregencopyThread.startworldregen(args[1]);
				return true;
			} else if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
				//reload
				config.load();
				configmsg.loadmsg();
				plugin.sendMessage(sender, "[AutoSaveWorld] All configurations reloaded");
				return true;
			} else if (args.length == 1 && args[0].equalsIgnoreCase("reloadconfig")) {
				//reload config
				config.load();
				plugin.sendMessage(sender,"[AutoSaveWorld] Main configuration reloaded");
				return true;
			} else if (args.length == 1 && args[0].equalsIgnoreCase("reloadmsg")) {
				//reload messages
				configmsg.loadmsg();
				plugin.sendMessage(sender, "[AutoSaveWorld] Messages file reloaded");
				return true;
			} else if (args.length == 1 && args[0].equalsIgnoreCase("version")) {
				//version
				plugin.sendMessage(sender, plugin.getDescription().getName()+ " " + plugin.getDescription().getVersion());
				return true;
			} else if (args.length == 1 && args[0].equalsIgnoreCase("info")) {
				//info
				plugin.sendMessage(sender,"&9======AutoSaveWorld Info & Status======");
				if (config.saveEnabled) {
					plugin.sendMessage(sender, "&2AutoSave is active");
					plugin.sendMessage(sender, "&2Last save time: " + plugin.LastSave);
				} else {
					plugin.sendMessage(sender, "&2AutoSave is inactive");
				}
				if (config.backupEnabled) {
					plugin.sendMessage(sender, "&2AutoBackup is active");
					plugin.sendMessage(sender, "&2Last backup time: " + plugin.LastBackup);
				} else {
					plugin.sendMessage(sender, "&2AutoBackup is inactive");
				}
				plugin.sendMessage(sender,"&9====================================");
				return true;
			} else if ((args.length >= 1 && args[0].equalsIgnoreCase("locale"))) {
				//locale loader
				if (args.length == 2 && args[1].equalsIgnoreCase("available")) {
					plugin.sendMessage(sender, "Available locales: "+ localeChanger.getAvailableLocales());
					return true;
				} else if (args.length == 2 && args[1].equalsIgnoreCase("load")) {
					plugin.sendMessage(sender,"You should specify a locale to load (get available locales using /asw locale available command)");
					return true;
				} else if (args.length == 3 && args[1].equalsIgnoreCase("load")) {
					if (localeChanger.getAvailableLocales().contains(args[2])) {
						plugin.sendMessage(sender, "Loading locale " + args[2]);
						localeChanger.loadLocale(args[2]);
						plugin.sendMessage(sender, "Loaded locale " + args[2]);
						return true;
					} else {
						plugin.sendMessage(sender, "Locale " + args[2] + " is not available");
						return true;
					}
				}
			}
			return false;
		}
		return false;
	}
}
