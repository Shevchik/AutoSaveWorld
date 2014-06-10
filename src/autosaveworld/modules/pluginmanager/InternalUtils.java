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
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.UnknownDependencyException;

public class InternalUtils {

	@SuppressWarnings("unchecked")
	protected void unloadPlugin(Plugin plugin) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, IOException, InterruptedException, NoSuchMethodException, InvocationTargetException {
		PluginManager pluginmanager = Bukkit.getPluginManager();
		Class<? extends PluginManager> managerclass = pluginmanager.getClass();
		//disable plugin
		try {
			pluginmanager.disablePlugin(plugin);
		} catch (Exception e) {
		}
		//remove from plugins field
		Field pluginsField = managerclass.getDeclaredField("plugins");
		pluginsField.setAccessible(true);
		List<Plugin> plugins = (List<Plugin>) pluginsField.get(pluginmanager);
		plugins.remove(plugin);
		//remove from lookupnames field
		Field lookupNamesField = managerclass.getDeclaredField("lookupNames");
		lookupNamesField.setAccessible(true);
		Map<String, Plugin> lookupNames = (Map<String, Plugin>) lookupNamesField.get(pluginmanager);
		lookupNames.remove(plugin.getName());
		//remove from commands field
		Field commandMapField = managerclass.getDeclaredField("commandMap");
		commandMapField.setAccessible(true);
		CommandMap commandMap = (CommandMap) commandMapField.get(pluginmanager);
		Method getCommandsMethod = commandMap.getClass().getMethod("getCommands");
		getCommandsMethod.setAccessible(true);
		Collection<Command> commands = (Collection<Command>) getCommandsMethod.invoke(commandMap);
		System.out.println(Bukkit.getServer().getClass().getProtectionDomain().getCodeSource().getLocation().getFile());
		for (Command cmd : new LinkedList<Command>(commands)) {
			if (cmd instanceof PluginIdentifiableCommand) {
				PluginIdentifiableCommand plugincommand = (PluginIdentifiableCommand) cmd;
				if (plugincommand.getPlugin().getName().equalsIgnoreCase(plugin.getName())) {
					cmd.unregister(commandMap);
					removeCommand(commands, cmd);
				}
			} else  if (cmd.getClass().getClassLoader() == plugin.getClass().getClassLoader()) {
				cmd.unregister(commandMap);
				removeCommand(commands, cmd);
			}
		}
		//close file in url classloader
		ClassLoader pluginClassLoader = plugin.getClass().getClassLoader();
		if (pluginClassLoader instanceof URLClassLoader) {
			URLClassLoader urlloader= (URLClassLoader) pluginClassLoader;
			urlloader.close();
		}
		//force gc
		System.gc();
		System.gc();
	}

	private void removeCommand(Collection<Command> commands, Command cmd) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		if (commands.getClass().getSimpleName().equals("UnmodifiableCollection")) {
			removeFromUnmodifiableCollection(commands, cmd);
		} else {
			commands.remove(cmd);
		}
	}

	private void removeFromUnmodifiableCollection(Collection<?> collection, Object toremove) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field originalField = collection.getClass().getDeclaredField("c");
		originalField.setAccessible(true);
		@SuppressWarnings("unchecked")
		Collection<Command> original = (Collection<Command>) originalField.get(collection);
		original.remove(toremove);
	}

	protected void loadPlugin(File pluginfile) throws UnknownDependencyException, InvalidPluginException, InvalidDescriptionException {
		PluginManager pluginmanager = Bukkit.getPluginManager();
		//load plugin
		Plugin plugin = pluginmanager.loadPlugin(pluginfile);
		//enable plugin
		plugin.onLoad();
		pluginmanager.enablePlugin(plugin);
	}

}
