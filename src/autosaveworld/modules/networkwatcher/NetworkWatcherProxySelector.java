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

package autosaveworld.modules.networkwatcher;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import sun.reflect.CallerSensitive;
import autosaveworld.config.AutoSaveWorldConfig;
import autosaveworld.core.logging.MessageLogger;

public class NetworkWatcherProxySelector extends ProxySelector {

	private AutoSaveWorldConfig config;
	private ProxySelector defaultSelector;

	public ProxySelector getDefaultSelector() {
		return defaultSelector;
	}

	public NetworkWatcherProxySelector(ProxySelector defaultSelector, AutoSaveWorldConfig config) {
		this.defaultSelector = defaultSelector;
		this.config = config;
	}

	@Override
	public List<Proxy> select(URI uri) {
		if (config.networkWatcher) {
			if (Bukkit.isPrimaryThread()) {
				Plugin plugin = getRequestingPlugin();
				if (plugin != null) {
					MessageLogger.warn("Plugin "+plugin.getName()+" attempted to establish connection "+uri+" in main server thread");
				} else {
					MessageLogger.warn("Something attempted to access "+uri+" in main server thread, printing stack trace");
					Thread.dumpStack();
				}
			}
		}
		return defaultSelector.select(uri);
	}

	@Override
	public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
		defaultSelector.connectFailed(uri, sa, ioe);
	}

	@CallerSensitive
	private Plugin getRequestingPlugin() {
		HashMap<ClassLoader, Plugin> map = getClassloaderToPluginMap();
		StackTraceElement[] stacktrace = new Exception().getStackTrace();
		for (int i = 0; i < stacktrace.length; i++) {
			StackTraceElement element = stacktrace[i];
			try {
				ClassLoader loader = Class.forName(element.getClassName(), false, getClass().getClassLoader()).getClassLoader();
				if (map.containsKey(loader)) {
					return map.get(loader);
				}
			} catch (ClassNotFoundException e) {
			}
		}
		return null;
	}

	private HashMap<ClassLoader, Plugin> getClassloaderToPluginMap() {
		HashMap<ClassLoader, Plugin> map = new HashMap<ClassLoader, Plugin>();
		for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
			map.put(plugin.getClass().getClassLoader(), plugin);
		}
		map.remove(getClass().getClassLoader());
		return map;
	}

}
