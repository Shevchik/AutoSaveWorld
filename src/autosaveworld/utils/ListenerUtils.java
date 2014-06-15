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

package autosaveworld.utils;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import autosaveworld.core.AutoSaveWorld;

public class ListenerUtils {

	private static AutoSaveWorld plugin;

	public static void init(AutoSaveWorld plugin) {
		ListenerUtils.plugin = plugin;
	}

	public static void registerListener(Listener l) {
		Bukkit.getPluginManager().registerEvents(l, plugin);
	}

	public static void unregisterListener(Listener l) {
		HandlerList.unregisterAll(l);
	}

}
