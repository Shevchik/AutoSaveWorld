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

package autosaveworld.core;

import java.io.File;

public class GlobalConstants {

	public static void init(AutoSaveWorld plugin) {
		pluginfolder = plugin.getDataFolder().getPath() + File.separator;
	}

	// main
	private static String pluginfolder;

	public static String getAutoSaveWorldFolder() {
		return pluginfolder;
	}

	public static String getPluginsFolder() {
		return new File(pluginfolder).getParentFile().getPath();
	}

	// config
	private static String configfile = "config.yml";

	public static String getConfigPath() {
		return getAutoSaveWorldFolder() + configfile;
	}

	private static String configmsgfile = "configmsg.yml";

	public static String getConfigMSGPath() {
		return getAutoSaveWorldFolder() + configmsgfile;
	}

}
