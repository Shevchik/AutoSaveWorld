package autosaveworld.pluginmanager;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import autosaveworld.core.AutoSaveWorld;

public class ASWPluginManager {
	
	private AutoSaveWorld plugin;
	public ASWPluginManager(AutoSaveWorld plugin)
	{
		this.plugin = plugin;
	}
	
	private InternalUtils iutils = new InternalUtils();

	public void handlePluginManagerCommand(CommandSender sender, String command, String pluginname)
	{
		if (command.equalsIgnoreCase("load"))
		{
			loadPlugin(sender,pluginname);
		} else
		if (command.equalsIgnoreCase("unload"))
		{
			unloadPlugin(sender,pluginname);
		} else
		if (command.equalsIgnoreCase("reload"))
		{
			unloadPlugin(sender,pluginname);
			loadPlugin(sender,pluginname);
		}
		else
		{
			sender.sendMessage("[AutoSaveWorld] Invalid plugin manager command");
		}
	}
	
	private void unloadPlugin(CommandSender sender, String pluginname)
	{
		Plugin pmplugin = Bukkit.getPluginManager().getPlugin(pluginname);
		if (pmplugin != null)
		{
			try {
				iutils.unloadPlugin(pmplugin);
				sender.sendMessage("[AutoSaveWorld] Plugin unloaded");
			} catch (Exception e) {
				sender.sendMessage("[AutoSaveWorld] Some error uccured while loading plugin");
			}
		} else
		{
			sender.sendMessage("[AutoSaveWorld] Plugin with this name not found");
		}
	}
	
	private void loadPlugin(CommandSender sender, String pluginname)
	{
		File pmpluginfile = new File(plugin.getDataFolder().getParent()+File.separator+pluginname+".jar");
		if (pmpluginfile.exists())
		{
			try {
				iutils.loadPlugin(pmpluginfile);
				sender.sendMessage("[AutoSaveWorld] Plugin loaded");
			} catch (Exception e) {
				sender.sendMessage("[AutoSaveWorld] Some error uccured while loading plugin");
			}
		} else
		{
			sender.sendMessage("[AutoSaveWorld] File with this plugin name not found");
		}
	}

}
