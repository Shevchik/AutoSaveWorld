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

package autosaveworld.modules.pluginmanager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import autosaveworld.core.GlobalConstants;
import autosaveworld.core.logging.MessageLogger;
import autosaveworld.utils.FileUtils;
import autosaveworld.utils.StringUtils;

public class PluginManager {

	private InternalUtils iutils = new InternalUtils();

	public void handlePluginManagerCommand(CommandSender sender, String command, String arg) {
		if (command.equalsIgnoreCase("load")) {
			loadPlugin(sender, arg);
		} else if (command.equalsIgnoreCase("unload")) {
			unloadPlugin(sender, arg);
		} else if (command.equalsIgnoreCase("reload")) {
			reloadPlugin(sender, arg);
		} else if (command.equalsIgnoreCase("removeperm")) {
			removePermissions(sender, arg.split("[ ]"));
		} else {
			MessageLogger.sendMessage(sender, "Invalid plugin manager command");
		}
	}

	private List<String> cmds = Arrays.asList(new String[] {"load", "unload", "reload", "removeperm"});
	public List<String> getTabComplete(CommandSender sender, String[] args) {
		if (args.length == 1) {
			ArrayList<String> result = new ArrayList<String>();
			for (String command : cmds) {
				if (command.startsWith(args[0])) {
					result.add(command);
				}
			}
			return result;
		}
		if (args.length >= 2) {
			if (args[0].equalsIgnoreCase("unload") || args[0].equalsIgnoreCase("reload")) {
				String input = StringUtils.join(Arrays.copyOfRange(args, 1, args.length), " ");
				ArrayList<String> result = new ArrayList<String>();
				for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
					if (plugin.getName().startsWith(input) && getOtherDependingPlugins(plugin).isEmpty()) {
						result.add(plugin.getName());
					}
				}
				return result;
			}
			if (args[0].equalsIgnoreCase("load")) {
				String input = StringUtils.join(Arrays.copyOfRange(args, 1, args.length), " ");
				ArrayList<String> result = new ArrayList<String>();
				for (File pluginfile : FileUtils.safeListFiles(new File(GlobalConstants.getPluginsFolder()))) {
					String pluginName = getPluginName(pluginfile);
					if (
						(pluginName != null) &&
						(pluginName.startsWith(input) || pluginName.replace(" ", "_").startsWith(input)) &&
						Bukkit.getPluginManager().getPlugin(pluginName) == null
					) {
						result.add(pluginName);
					}
				}
				return result;
			}
		}
		return Collections.emptyList();
	}

	private void removePermissions(CommandSender sender, String... permissions) {
		for (String permission : permissions) {
			Bukkit.getPluginManager().removePermission(permission);
		}
		MessageLogger.sendMessage(sender, "Permission ungregistered");
	}

	private void loadPlugin(CommandSender sender, String pluginname) {
		// ignore if plugin is already loaded
		if (isPluginAlreadyLoaded(pluginname)) {
			MessageLogger.sendMessage(sender, "Plugin is already loaded");
			return;
		}
		// find plugin file
		File pmpluginfile = findPluginFile(pluginname);
		// ignore if we can't find plugin file
		if (!pmpluginfile.exists()) {
			MessageLogger.sendMessage(sender, "File with this plugin name not found");
			return;
		}
		// now load plugin
		try {
			iutils.loadPlugin(pmpluginfile);
			MessageLogger.sendMessage(sender, "Plugin loaded");
		} catch (Exception e) {
			e.printStackTrace();
			MessageLogger.sendMessage(sender, "Some error occured while loading plugin");
		}
	}

	private void unloadPlugin(CommandSender sender, String pluginname) {
		// find plugin
		Plugin pmplugin = findPlugin(pluginname);
		// ignore if plugin is not loaded
		if (pmplugin == null) {
			MessageLogger.sendMessage(sender, "Plugin with this name not found");
			return;
		}
		// check if plugin has other active depending plugins
		List<String> depending = getOtherDependingPlugins(pmplugin);
		if (!depending.isEmpty()) {
			MessageLogger.sendMessage(sender, "Found other plugins that depend on this one, disable them first: "+StringUtils.join(depending.toArray(new String[depending.size()]), ", "));
			return;
		}
		// now unload plugin
		try {
			iutils.unloadPlugin(pmplugin);
			MessageLogger.sendMessage(sender, "Plugin unloaded");
		} catch (Exception e) {
			e.printStackTrace();
			MessageLogger.sendMessage(sender, "Some error occured while unloading plugin");
		}
	}

	private void reloadPlugin(CommandSender sender, String pluginname) {
		// find plugin
		Plugin pmplugin = findPlugin(pluginname);
		// ignore if plugin is not loaded
		if (pmplugin == null) {
			MessageLogger.sendMessage(sender, "Plugin with this name not found");
			return;
		}
		// check if plugin has other active depending plugins
		List<String> depending = getOtherDependingPlugins(pmplugin);
		if (!depending.isEmpty()) {
			MessageLogger.sendMessage(sender, "Found other plugins that depend on this one, disable them first: "+StringUtils.join(depending.toArray(new String[depending.size()]), ", "));
			return;
		}
		// find plugin file
		File pmpluginfile = findPluginFile(pluginname);
		// ignore if we can't find plugin file
		if (!pmpluginfile.exists()) {
			MessageLogger.sendMessage(sender, "File with this plugin name not found");
			return;
		}
		// now reload plugin
		try {
			iutils.unloadPlugin(pmplugin);
			iutils.loadPlugin(pmpluginfile);
			MessageLogger.sendMessage(sender, "Plugin reloaded");
		} catch (Exception e) {
			e.printStackTrace();
			MessageLogger.sendMessage(sender, "Some error occured while reloading plugin");
		}
	}


	private Plugin findPlugin(String pluginname) {
		for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
			if (plugin.getName().equalsIgnoreCase(pluginname)) {
				return plugin;
			}
		}
		return Bukkit.getPluginManager().getPlugin(pluginname);
	}

	private File findPluginFile(String pluginname) {
		for (File pluginfile : FileUtils.safeListFiles(new File(GlobalConstants.getPluginsFolder()))) {
			String pluginName = getPluginName(pluginfile);
			if ((pluginName != null) && (pluginname.equalsIgnoreCase(pluginName) || pluginname.equalsIgnoreCase(pluginName.replace(" ", "_")))) {
				return pluginfile;
			}
		}
		return new File(GlobalConstants.getPluginsFolder(), pluginname + ".jar");
	}

	private String getPluginName(File pluginfile) {
		if (pluginfile.getName().endsWith(".jar")) {
			try (final JarFile jarFile = new JarFile(pluginfile)) {
				JarEntry je = jarFile.getJarEntry("plugin.yml");
				if (je != null) {
					PluginDescriptionFile plugininfo = new PluginDescriptionFile(jarFile.getInputStream(je));
					String jarpluginName = plugininfo.getName();
					jarFile.close();
					return jarpluginName;
				}
				jarFile.close();
			} catch (IOException | InvalidDescriptionException e) {
			}
		}
		return null;
	}


	private List<String> getOtherDependingPlugins(Plugin plugin) {
		ArrayList<String> others = new ArrayList<String>();
		for (Plugin otherplugin : Bukkit.getPluginManager().getPlugins()) {
			if (otherplugin.getDescription().getDepend().contains(plugin.getName()) || otherplugin.getDescription().getSoftDepend().contains(plugin.getName())) {
				others.add(otherplugin.getName());
			}
		}
		return others;
	}

	private boolean isPluginAlreadyLoaded(String pluginname) {
		for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
			if (plugin.getName().equalsIgnoreCase(pluginname)) {
				return true;
			}
		}
		return false;
	}

}
