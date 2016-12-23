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
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.SyncFailedException;
import java.io.Writer;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import autosaveworld.core.AutoSaveWorld;
import autosaveworld.utils.BukkitUtils;

public class MessageLogger {

	private static final FormattingCodesParser formattingCodesParser = new FormattingCodesParser();

	public static void sendMessage(CommandSender sender, String message) {
		if (!message.equals("")) {
			sender.sendMessage(formattingCodesParser.parseFormattingCodes(message));
		}
	}

	public static void sendExceptionMessage(final CommandSender sender, String message, Throwable t) {
		sender.sendMessage(formattingCodesParser.parseFormattingCodes(message));
		t.printStackTrace(new PrintWriter(new Writer() {
			@Override
			public void write(char[] cbuf, int off, int len) throws IOException {
				sender.sendMessage(new String(cbuf, off, len));
			}
			@Override
			public void flush() throws IOException {
			}
			@Override
			public void close() throws IOException {
			}
		}));
	}

	public static void broadcast(String message, boolean broadcast) {
		if (!message.equals("") && broadcast) {
			message = formattingCodesParser.parseFormattingCodes(message);
			for (Player player : BukkitUtils.getOnlinePlayers()) {
				player.sendMessage(message);
			}
			Bukkit.getConsoleSender().sendMessage(message);
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
		if (AutoSaveWorld.getInstance().getMainConfig().varDebug) {
			AutoSaveWorld.getInstance().getLogger().info(formattingCodesParser.stripFormattingCodes(message));
		}
	}

	public static void exception(String message, Throwable t) {
		AutoSaveWorld.getInstance().getLogger().log(Level.SEVERE, message, t);
	}

	public static void warn(String message) {
		AutoSaveWorld.getInstance().getLogger().warning(formattingCodesParser.stripFormattingCodes(message));
	}

	private static final PrintStream outstream = new PrintStream(new FileOutputStream(FileDescriptor.err), true);

	public static void printOutException(Throwable t) {
		t.printStackTrace(outstream);
		try {
			FileDescriptor.err.sync();
		} catch (SyncFailedException e) {
		}
	}

	public static void printOut(String message) {
		if (AutoSaveWorld.getInstance().getMainConfig().varDebug) {
			outstream.println("[AutoSaveWorld] "+message);
			try {
				FileDescriptor.err.sync();
			} catch (SyncFailedException e) {
			}
		}
	}

}
