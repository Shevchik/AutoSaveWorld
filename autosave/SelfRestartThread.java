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
package autosave;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.SimplePluginManager;

public class SelfRestartThread  extends Thread{
	
	private AutoSave plugin;
	private boolean run = true;
	public boolean restart = false;
	
	protected final Logger log = Logger.getLogger("Minecraft");
	
	SelfRestartThread(AutoSave plugin)
	{
		this.plugin = plugin;
	}
	public void stopthread() {
	this.run = false;
	}
	public void restart()
	{restart=true;}
	@SuppressWarnings("unchecked")
	public void run()
	{
		log.info("[AutoSaveWorld] SelfRestartThread started");
		while (run)
		{
		try {Thread.sleep(3000);} catch (InterruptedException e) {e.printStackTrace();}
			if (restart) {
				//HAAAAAAAX
				try {
					PluginManager pluginmanager = Bukkit.getPluginManager();
					SimplePluginManager  spluginmanager = (SimplePluginManager) pluginmanager;
        
					Field pluginsField = spluginmanager.getClass().getDeclaredField("plugins");
					pluginsField.setAccessible(true);
					List<Plugin> plugins = (List<Plugin>) pluginsField.get(spluginmanager);
        
					Field lookupNamesField = spluginmanager.getClass().getDeclaredField("lookupNames");
					lookupNamesField.setAccessible(true);
					Map<String, Plugin> lookupNames = (Map<String, Plugin>) lookupNamesField.get(spluginmanager);

					Field commandMapField = spluginmanager.getClass().getDeclaredField("commandMap");
					commandMapField.setAccessible(true);
					SimpleCommandMap commandMap = (SimpleCommandMap) commandMapField.get(spluginmanager);
					Field knownCommandsField = commandMap.getClass().getDeclaredField("knownCommands");
					knownCommandsField.setAccessible(true);
					Map<String, Command> knownCommands = (Map<String, Command>) knownCommandsField.get(commandMap);
        
					plugin.debug("Starting self restart and update");
					log.info("[AutoSaveWorld] Disabling self");
					//disable AutoSaveWorld
					pluginmanager.disablePlugin(plugin);
					//unload AutoSaveWorld

					if (plugins != null && plugins.contains(plugin)) {
						plugins.remove(plugin);
					}
					//remove lookupnames
					if (lookupNames != null && lookupNames.containsKey("AutoSaveWorld")) {
						lookupNames.remove("AutoSaveWorld");
					}
					//unregister commands
					if (commandMap != null) {
						for (Iterator<Map.Entry<String, Command>> it = knownCommands.entrySet().iterator(); it.hasNext();) {
							Map.Entry<String, Command> entry = it.next();

							if (entry.getValue() instanceof PluginCommand) {
								PluginCommand command = (PluginCommand) entry.getValue();

								if (command.getPlugin() == plugin) {
									command.unregister(commandMap);
									it.remove();
								}
							}
						}
					}
					log.info("[AutoSaveWorld] Disabled self");
					//unload finished
					//enable AutoSaveWorld
					log.info("[AutoSaveWorld] Starting self");
					//load plugin from folder
					Plugin aswplugin = pluginmanager.loadPlugin(new File(new File(".").getCanonicalPath()+File.separator+"plugins"+File.separator+"AutoSaveWorld.jar"));
					//load plugin
					pluginmanager.enablePlugin(aswplugin);
					log.info("[AutoSaveWorld] Started self");
					run = false;
					//we are done here
				} catch (Exception e) {e.printStackTrace();
				log.info("[AutoSaveWorld] &4AutoSaveWorld selfreload failed, plugin is probably not working.");
				log.info("[AutoSaveWorld] &4Restart server to fix this and report this stacktrace to AutoSaveWorld author");
				}
			}
		}
		//if we reached this part and restart is true, then the restart occured and we no longer need this Thread. 
		if (restart) {
			try {
				this.join(5000);
				log.info("[AutoSaveWorld] Quit of old AutoSaveWorld selfrestart thread");
			} catch (InterruptedException e) {e.printStackTrace();}
		}
	}
}
