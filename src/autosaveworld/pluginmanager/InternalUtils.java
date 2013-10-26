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

package autosaveworld.pluginmanager;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.UnknownDependencyException;
import org.bukkit.plugin.java.PluginClassLoader;

public class InternalUtils {

	@SuppressWarnings("unchecked")
	protected void unloadPlugin(Plugin plugin) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, IOException, InterruptedException
	{
		PluginManager pluginmanager = Bukkit.getPluginManager();
		Class<? extends PluginManager> managerclass = pluginmanager.getClass();
		//disable plugin
		pluginmanager.disablePlugin(plugin);
		//remove from plugins field
		Field pluginsField = managerclass.getDeclaredField("plugins");
		pluginsField.setAccessible(true);
		List<Plugin> plugins = (List<Plugin>) pluginsField.get(pluginmanager);
		plugins.remove(plugin);
		//remove from lookupnames
		Field lookupNamesField = managerclass.getDeclaredField("lookupNames");
		lookupNamesField.setAccessible(true);
		Map<String, Plugin> lookupNames = (Map<String, Plugin>) lookupNamesField.get(pluginmanager);
		lookupNames.remove(plugin.getName());
		//remove from command fields
		Field commandMapField = managerclass.getDeclaredField("commandMap");
		commandMapField.setAccessible(true);
		CommandMap commandMap = (CommandMap) commandMapField.get(pluginmanager);
		Field knownCommandsField = null;
		Map<String, Command> knownCommands = null;
		knownCommandsField = commandMap.getClass().getDeclaredField("knownCommands");
		knownCommandsField.setAccessible(true);
		knownCommands = (Map<String, Command>) knownCommandsField.get(commandMap);
		for (String plugincommandName : new HashSet<String>(knownCommands.keySet()))
		{
			if (knownCommands.get(plugincommandName) instanceof PluginCommand)
			{
				PluginCommand plugincommand = (PluginCommand) knownCommands.get(plugincommandName);
				if (plugincommand.getPlugin().getName().equals(plugin.getName()))
				{
					plugincommand.unregister(commandMap);
					knownCommands.remove(plugincommandName);
				}
			}
		}
		//close file in url classloader and then kill classloader
		ClassLoader pluginClassLoader = plugin.getClass().getClassLoader();
		if (pluginClassLoader instanceof PluginClassLoader && pluginClassLoader instanceof URLClassLoader)
		{
			URLClassLoader urlloader= (URLClassLoader.class.cast(PluginClassLoader.class.cast(pluginClassLoader)));
			urlloader.close();
		}
		System.gc();
	}
	
	protected void loadPlugin(File pluginfile) throws UnknownDependencyException, InvalidPluginException, InvalidDescriptionException
	{
		PluginManager pluginmanager = Bukkit.getPluginManager();
		//load plugin
		Plugin plugin = Bukkit.getPluginManager().loadPlugin(pluginfile);
		//enable plugin
		plugin.onLoad();
		pluginmanager.enablePlugin(plugin);
	}
	
}
