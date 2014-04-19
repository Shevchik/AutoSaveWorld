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

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

import autosaveworld.config.AutoSaveWorldConfig;

public class MessageLogger {
	
	private static AutoSaveWorldConfig config;
	private static Logger log;
	private static FormattingCodesParser formattingCodesParser = new FormattingCodesParser();
	public MessageLogger(Logger log, AutoSaveWorldConfig config) {
		MessageLogger.log = log;
		MessageLogger.config = config;
	}

	public static void sendMessage(CommandSender sender, String message) {
		if (!message.equals("")) {
			if (formattingCodesParser != null) {
				sender.sendMessage(formattingCodesParser.parseFormattingCodes(message));
			}
		}
	}

	public static void broadcast(String message, boolean broadcast) {
		if (!message.equals("") && broadcast) {
			if (formattingCodesParser != null) {
				Bukkit.broadcastMessage(formattingCodesParser.parseFormattingCodes(message));
			}
		}
	}

	public static void kickPlayer(Player player, String message) {
		if (formattingCodesParser != null) {
			player.kickPlayer(formattingCodesParser.parseFormattingCodes(message));
		}
	}

	public static void disallow(PlayerLoginEvent e, String message) {
		if (formattingCodesParser != null) {
			e.disallow(Result.KICK_OTHER, formattingCodesParser.parseFormattingCodes(message));
		}
	}

	public static void debug(String message) {
		if (config != null && config.varDebug) {
			if (formattingCodesParser != null) {
				log.info(formattingCodesParser.stripFormattingCodes(message));
			}
		}
	}

	public static void warn(String message) {
		if (log != null) {
			if (formattingCodesParser != null) {
				log.warning(formattingCodesParser.stripFormattingCodes(message));
			}
		}
	}

}
