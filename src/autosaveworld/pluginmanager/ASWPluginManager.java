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
				e.printStackTrace();
				sender.sendMessage("[AutoSaveWorld] Some error occured while loading plugin");
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
				e.printStackTrace();
				sender.sendMessage("[AutoSaveWorld] Some error occured while unloading plugin");
			}
		} else
		{
			sender.sendMessage("[AutoSaveWorld] File with this plugin name not found");
		}
	}

}
