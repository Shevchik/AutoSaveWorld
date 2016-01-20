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

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.SyncFailedException;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import autosaveworld.config.AutoSaveWorldConfig;

public class MessageLogger {

	private static final FormattingCodesParser formattingCodesParser = new FormattingCodesParser();

	private static AutoSaveWorldConfig config;
	private static Logger log;

	public static void init(Logger log, AutoSaveWorldConfig config) {
		MessageLogger.log = log;
		MessageLogger.config = config;
	}

	public static void sendMessage(CommandSender sender, String message) {
		if (!message.equals("")) {
			sender.sendMessage(formattingCodesParser.parseFormattingCodes(message));
		}
	}

	public static void broadcast(String message, boolean broadcast) {
		if (!message.equals("") && broadcast) {
			try {
				Bukkit.broadcastMessage(formattingCodesParser.parseFormattingCodes(message));
			} catch (Throwable t) {
			}
		}
	}

	public static void kickPlayer(Player player, String message) {
		player.kickPlayer(formattingCodesParser.parseFormattingCodes(message));
	}

	public static void disallow(AsyncPlayerPreLoginEvent e, String message) {
		e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, formattingCodesParser.parseFormattingCodes(message));
	}

	public static void disallow(PlayerLoginEvent e, String message) {
		e.disallow(PlayerLoginEvent.Result.KICK_OTHER, formattingCodesParser.parseFormattingCodes(message));
	}

	public static void debug(String message) {
		if (config != null && config.varDebug && log != null) {
			log.info(formattingCodesParser.stripFormattingCodes(message));
		}
	}

	public static void warn(String message) {
		if (log != null) {
			log.warning(formattingCodesParser.stripFormattingCodes(message));
		}
	}

	private static final PrintStream outstream = new PrintStream(new FileOutputStream(FileDescriptor.err), true);

	public static void printOutException(Throwable t) {
		t.printStackTrace(outstream);
		try {
			FileDescriptor.err.sync();
		} catch (SyncFailedException e) {
		}
	}

	public static void printOutDebug(String message) {
		if (config != null && config.varDebug) {
			printOut(message);
		}
	}

	public static void printOut(String message) {
		if (config != null && config.varDebug) {
			outstream.println("[AutoSaveWorld] "+message);
			try {
				FileDescriptor.err.sync();
			} catch (SyncFailedException e) {
			}
		}
	}

}
