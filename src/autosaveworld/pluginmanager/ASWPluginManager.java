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
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
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
		//find plugin
		Plugin pmplugin = findPlugin(pluginname);
		//ignore if plugin is not loaded
		if (pmplugin == null)
		{
			sender.sendMessage("[AutoSaveWorld] Plugin with this name not found");
			return;
		}
		//now unload plugin
		try {
			iutils.unloadPlugin(pmplugin);
			sender.sendMessage("[AutoSaveWorld] Plugin unloaded");
		} catch (Exception e) {
			e.printStackTrace();
			sender.sendMessage("[AutoSaveWorld] Some error occured while loading plugin");
		}
	}
	
	private void loadPlugin(CommandSender sender, String pluginname)
	{
		//ignore if plugin is already loaded
		if (isPluginAlreadyLoaded(pluginname)) 
		{
			sender.sendMessage("[AutoSaveWorld] Plugin is alreadt loaded");
			return;
		}
		//find plugin file
		File pmpluginfile = findPluginFile(pluginname);
		//ignore if we can't find plugin file
		if (!pmpluginfile.exists())
		{
			sender.sendMessage("[AutoSaveWorld] File with this plugin name not found");
			return;
		}
		//now load plugin
		try {
			iutils.loadPlugin(pmpluginfile);
			sender.sendMessage("[AutoSaveWorld] Plugin loaded");
		} catch (Exception e) {
			e.printStackTrace();
			sender.sendMessage("[AutoSaveWorld] Some error occured while unloading plugin");
		}
	}
	
	private Plugin findPlugin(String pluginname)
	{
		Plugin pmplugin = Bukkit.getPluginManager().getPlugin(pluginname);
		for (Plugin plugin : Bukkit.getPluginManager().getPlugins())
		{
			if (plugin.getName().equalsIgnoreCase(pluginname))
			{
				pmplugin = plugin;
				break;
			}
		}
		return pmplugin;
	}
	
	private File findPluginFile(String pluginname)
	{
		File pmpluginfile = new File(plugin.getDataFolder().getParent()+File.separator+pluginname+".jar");
		try {
			File pluginsfolder = plugin.getDataFolder().getParentFile();
			for (File pluginfile : pluginsfolder.listFiles())
			{
				boolean found = false;
				final JarFile jarFile = new JarFile(pluginfile);
				JarEntry je = jarFile.getJarEntry("plugin.yml");
				if (je != null)
				{
					FileConfiguration plugininfo = YamlConfiguration.loadConfiguration(jarFile.getInputStream(je));
					String jarpluginName = plugininfo.getString("name");
					if (pluginname.equalsIgnoreCase(jarpluginName))
					{
						pluginname = jarpluginName;
						found = true;
					}
				}
				jarFile.close();
				if (found)
				{
					break;
				}
			}
		} catch (Exception e) {}
		return pmpluginfile;
	}
	
	private boolean isPluginAlreadyLoaded(String pluginname)
	{
		for (Plugin plugin : Bukkit.getPluginManager().getPlugins())
		{
			if (plugin.getName().equalsIgnoreCase(pluginname))
			{
				return true;
			}
		}
		return false;
	}

}
