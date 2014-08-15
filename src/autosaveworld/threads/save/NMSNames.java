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

package autosaveworld.threads.save;

import org.bukkit.Bukkit;

public class NMSNames {

	public static String getDataManagerFieldName() {
		if (isCauldron()) {
			return "";
		} else {
			return "dataManager";
		}
	}

	public static String getCheckSessionMethodName() {
		if (isCauldron()) {
			return "";
		} else {
			return "checkSession";
		}
	}

	public static String getWorldDataFieldName() {
		if (isCauldron()) {
			return "";
		} else {
			return "worldData";
		}
	}

	public static String getChunkProviderFieldName() {
		if (isCauldron()) {
			return "";
		} else {
			return "chunkProvider";
		}
	}

	public static String getSaveWorldDataMethodName() {
		if (isCauldron()) {
			return "";
		} else {
			return "saveWorldData";
		}
	}

	public static String getSaveChunksMethodName() {
		if (isCauldron()) {
			return "";
		} else {
			return "saveChunks";
		}
	}

	public static String getSaveLevelMethodName() {
		if (isCauldron()) {
			return "";
		} else {
			return "saveLevel";
		}
	}

	private static boolean isCauldron() {
		String version = Bukkit.getVersion().toLowerCase();
		return (version.contains("mcpc") || version.contains("cauldron"));
	}

}
