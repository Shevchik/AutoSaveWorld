/**
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * 
 */

package autosaveworld;

import java.util.List;

import org.bukkit.ChatColor;

public class Generic {
	public static boolean stringArrayContains(String base, String[] comparesWith) {

		for (int i = 0; i < comparesWith.length; i++) {
			try {
				if (base.compareTo(comparesWith[i]) == 0) {
					return true;
				}
			} catch (NullPointerException npe) {
				return false;
			}
		}

		return false;
	}

	public static String join(String glue, List<?> s) {
		try {
			if (s == null) {
				return "";
			}

			int k = s.size();
			if (k == 0) {
				return null;
			}
			StringBuilder out = new StringBuilder();
			out.append(s.get(0).toString());
			for (int x = 1; x < k; ++x) {
				out.append(glue).append(s.get(x).toString());
			}
			return out.toString();
		} catch (NullPointerException npe) {
			return "";
		}
	}

	// Parse Colors
	public static String parseColor(String message) {
		message = message.replaceAll("&0", ChatColor.BLACK + "");
		message = message.replaceAll("&1", ChatColor.DARK_BLUE + "");
		message = message.replaceAll("&2", ChatColor.DARK_GREEN + "");
		message = message.replaceAll("&3", ChatColor.DARK_AQUA + "");
		message = message.replaceAll("&4", ChatColor.DARK_RED + "");
		message = message.replaceAll("&5", ChatColor.DARK_PURPLE + "");
		message = message.replaceAll("&6", ChatColor.GOLD + "");
		message = message.replaceAll("&7", ChatColor.GRAY + "");
		message = message.replaceAll("&8", ChatColor.DARK_GRAY + "");
		message = message.replaceAll("&9", ChatColor.BLUE + "");
		message = message.replaceAll("(?i)&a", ChatColor.GREEN + "");
		message = message.replaceAll("(?i)&b", ChatColor.AQUA + "");
		message = message.replaceAll("(?i)&c", ChatColor.RED + "");
		message = message.replaceAll("(?i)&d", ChatColor.LIGHT_PURPLE + "");
		message = message.replaceAll("(?i)&e", ChatColor.YELLOW + "");
		message = message.replaceAll("(?i)&f", ChatColor.WHITE + "");
		message = message.replaceAll("(?i)&l", ChatColor.BOLD+ "");
		message = message.replaceAll("(?i)&o", ChatColor.ITALIC+ "");
		message = message.replaceAll("(?i)&m", ChatColor.STRIKETHROUGH+ "");
		message = message.replaceAll("(?i)&n", ChatColor.UNDERLINE+ "");
		message = message.replaceAll("(?i)&k", ChatColor.MAGIC+ "");
		return message;
	}

	// Parse Colors
	public static String stripColor(String message) {
		message = message.replaceAll("&0", "");
		message = message.replaceAll("&1", "");
		message = message.replaceAll("&2", "");
		message = message.replaceAll("&3", "");
		message = message.replaceAll("&4", "");
		message = message.replaceAll("&5", "");
		message = message.replaceAll("&6", "");
		message = message.replaceAll("&7", "");
		message = message.replaceAll("&8", "");
		message = message.replaceAll("&9", "");
		message = message.replaceAll("(?i)&a", "");
		message = message.replaceAll("(?i)&b", "");
		message = message.replaceAll("(?i)&c", "");
		message = message.replaceAll("(?i)&d", "");
		message = message.replaceAll("(?i)&e", "");
		message = message.replaceAll("(?i)&f", "");
		message = message.replaceAll("(?i)&l", "");
		message = message.replaceAll("(?i)&o", "");
		message = message.replaceAll("(?i)&m", "");
		message = message.replaceAll("(?i)&n", "");
		message = message.replaceAll("(?i)&k", "");
		return message;
	}
}
