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

package autosaveworld.listener;


import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.RemoteServerCommandEvent;
import org.bukkit.event.server.ServerCommandEvent;

import autosaveworld.core.AutoSaveWorld;

	public class EventsListener implements Listener {

		private AutoSaveWorld plugin = null;
		public EventsListener(AutoSaveWorld plugin){
			this.plugin = plugin;

		};
		
		
		//save when last player quits
		@EventHandler
		public void onPlayerQuit(PlayerQuitEvent event) {
			plugin.debug("Check for last leave");
			plugin.debug("Players online = "+(plugin.getServer().getOnlinePlayers().length-1));
			if (plugin.getServer().getOnlinePlayers().length == 1) {
				plugin.debug("Last player has quit, autosaving");
				plugin.saveThread.startsave();
			}
		}
		
		//stop crashrestart immediately when somebody issues stop command
		@EventHandler(priority=EventPriority.MONITOR,ignoreCancelled=true)
		public void onConsoleStopCommand(ServerCommandEvent event)
		{
			if (event.getCommand().equalsIgnoreCase("stop")||event.getCommand().equalsIgnoreCase("restart")||event.getCommand().equalsIgnoreCase("reload")) {
				plugin.crashrestartThread.stopThread();
			}
		}
		
		//stop crashrestart immediately when somebody issues stop command
		@EventHandler(priority=EventPriority.MONITOR,ignoreCancelled=true)
		public void onRemoteConsoleStopCommand(RemoteServerCommandEvent event)
		{
			if (event.getCommand().equalsIgnoreCase("stop")||event.getCommand().equalsIgnoreCase("restart")||event.getCommand().equalsIgnoreCase("reload")) {
				plugin.crashrestartThread.stopThread(); 
			}
		}
		
}