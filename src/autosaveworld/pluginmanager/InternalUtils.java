package autosaveworld.pluginmanager;

import java.io.File;
import java.lang.reflect.Field;
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

public class InternalUtils {

	@SuppressWarnings("unchecked")
	protected void unloadPlugin(Plugin plugin) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException
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
