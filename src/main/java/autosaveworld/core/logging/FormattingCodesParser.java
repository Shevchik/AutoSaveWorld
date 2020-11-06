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

package autosaveworld.core.logging;

import org.bukkit.ChatColor;

public class FormattingCodesParser {

	public String parseFormattingCodes(String message) {
		return message
			.replace("&0", ChatColor.BLACK.toString())
			.replace("&1", ChatColor.DARK_BLUE.toString())
			.replace("&2", ChatColor.DARK_GREEN.toString())
			.replace("&3", ChatColor.DARK_AQUA.toString())
			.replace("&4", ChatColor.DARK_RED.toString())
			.replace("&5", ChatColor.DARK_PURPLE.toString())
			.replace("&6", ChatColor.GOLD.toString())
			.replace("&7", ChatColor.GRAY.toString())
			.replace("&8", ChatColor.DARK_GRAY.toString())
			.replace("&9", ChatColor.BLUE.toString())
			.replaceAll("(?i)&a", ChatColor.GREEN.toString())
			.replaceAll("(?i)&b", ChatColor.AQUA.toString())
			.replaceAll("(?i)&c", ChatColor.RED.toString())
			.replaceAll("(?i)&d", ChatColor.LIGHT_PURPLE.toString())
			.replaceAll("(?i)&e", ChatColor.YELLOW.toString())
			.replaceAll("(?i)&f", ChatColor.WHITE.toString())
			.replaceAll("(?i)&l", ChatColor.BOLD.toString())
			.replaceAll("(?i)&o", ChatColor.ITALIC.toString())
			.replaceAll("(?i)&m", ChatColor.STRIKETHROUGH.toString())
			.replaceAll("(?i)&n", ChatColor.UNDERLINE.toString())
			.replaceAll("(?i)&k", ChatColor.MAGIC.toString());
	}

	public String stripFormattingCodes(String message) {
		return message
			.replace("&0", "")
			.replace("&1", "")
			.replace("&2", "")
			.replace("&3", "")
			.replace("&4", "")
			.replace("&5", "")
			.replace("&6", "")
			.replace("&7", "")
			.replace("&8", "")
			.replace("&9", "")
			.replaceAll("(?i)&a", "")
			.replaceAll("(?i)&b", "")
			.replaceAll("(?i)&c", "")
			.replaceAll("(?i)&d", "")
			.replaceAll("(?i)&e", "")
			.replaceAll("(?i)&f", "")
			.replaceAll("(?i)&l", "")
			.replaceAll("(?i)&o", "")
			.replaceAll("(?i)&m", "")
			.replaceAll("(?i)&n", "")
			.replaceAll("(?i)&k", "");
	}

}
