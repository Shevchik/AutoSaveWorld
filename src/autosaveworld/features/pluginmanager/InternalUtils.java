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

package autosaveworld.features.pluginmanager;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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

import autosaveworld.utils.ReflectionUtils;

public class InternalUtils {

	@SuppressWarnings({ "unchecked", "deprecation" })
	protected void unloadPlugin(Plugin plugin) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, IOException, InterruptedException, NoSuchMethodException, InvocationTargetException {
		PluginManager pluginmanager = Bukkit.getPluginManager();
		Class<? extends PluginManager> managerclass = pluginmanager.getClass();
		ClassLoader pluginClassLoader = plugin.getClass().getClassLoader();
		// disable plugin
		pluginmanager.disablePlugin(plugin);
		// kill threads if any
		for (Thread thread : Thread.getAllStackTraces().keySet()) {
			if (thread.getClass().getClassLoader() == pluginClassLoader) {
				thread.interrupt();
				thread.join(2000);
				if (thread.isAlive()) {
					thread.stop();
				}
			}
		}
		// remove from plugins field
		((List<Plugin>) ReflectionUtils.getField(managerclass, "plugins").get(pluginmanager)).remove(plugin);
		// remove from lookupnames field
		((Map<String, Plugin>) ReflectionUtils.getField(managerclass, "lookupNames").get(pluginmanager)).values().remove(plugin);
		// remove from commands field
		CommandMap commandMap = (CommandMap) ReflectionUtils.getField(managerclass, "commandMap").get(pluginmanager);
		Collection<Command> commands = (Collection<Command>) ReflectionUtils.getMethod(commandMap.getClass(), "getCommands", 0).invoke(commandMap);
		for (Command cmd : new LinkedList<Command>(commands)) {
			if (cmd instanceof PluginIdentifiableCommand) {
				PluginIdentifiableCommand plugincommand = (PluginIdentifiableCommand) cmd;
				if (plugincommand.getPlugin().getName().equalsIgnoreCase(plugin.getName())) {
					removeCommand(commandMap, commands, cmd);
				}
			} else if (cmd.getClass().getClassLoader() == pluginClassLoader) {
				removeCommand(commandMap, commands, cmd);
			}
		}
		// close file in url classloader
		if (pluginClassLoader instanceof URLClassLoader) {
			URLClassLoader urlloader = (URLClassLoader) pluginClassLoader;
			urlloader.close();
		}
	}

	private void removeCommand(CommandMap commandMap, Collection<Command> commands, Command cmd) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		cmd.unregister(commandMap);
		if (commands.getClass().getSimpleName().equals("UnmodifiableCollection")) {
			Field originalField = commands.getClass().getDeclaredField("c");
			originalField.setAccessible(true);
			@SuppressWarnings("unchecked")
			Collection<Command> original = (Collection<Command>) originalField.get(commands);
			original.remove(cmd);
		} else {
			commands.remove(cmd);
		}
	}

	protected void loadPlugin(File pluginfile) throws UnknownDependencyException, InvalidPluginException, InvalidDescriptionException, IllegalArgumentException, IllegalAccessException {
		PluginManager pluginmanager = Bukkit.getPluginManager();
		// load plugin
		Plugin plugin = pluginmanager.loadPlugin(pluginfile);
		// enable plugin
		plugin.onLoad();
		pluginmanager.enablePlugin(plugin);
	}

}
