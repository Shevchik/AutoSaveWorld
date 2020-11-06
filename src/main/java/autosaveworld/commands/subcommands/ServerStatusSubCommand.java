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

package autosaveworld.commands.subcommands;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import autosaveworld.commands.ISubCommand;

public class ServerStatusSubCommand implements ISubCommand {

	@Override
	public void handle(CommandSender sender, String[] args) {
		DecimalFormat df = new DecimalFormat("0.00");
		// processor (if available)
		try {
			com.sun.management.OperatingSystemMXBean systemBean = (com.sun.management.OperatingSystemMXBean) java.lang.management.ManagementFactory.getOperatingSystemMXBean();
			double cpuusage = systemBean.getProcessCpuLoad() * 100;
			if (cpuusage > 0) {
				sender.sendMessage(ChatColor.GOLD + "Cpu usage: " + ChatColor.RED + df.format(cpuusage) + "%");
			} else {
				sender.sendMessage(ChatColor.GOLD + "Cpu usage: " + ChatColor.RED + "not available");
			}
		} catch (Throwable t) {
		}
		// memory
		Runtime runtime = Runtime.getRuntime();
		long maxmemmb = runtime.maxMemory() / 1024 / 1024;
		long freememmb = (runtime.maxMemory() - (runtime.totalMemory() - runtime.freeMemory())) / 1024 / 1024;
		sender.sendMessage(ChatColor.GOLD + "Memory usage: " + ChatColor.RED + df.format(((maxmemmb - freememmb) * 100) / maxmemmb) + "% " + ChatColor.DARK_AQUA + "(" + ChatColor.DARK_GREEN + (maxmemmb - freememmb) + "/" + maxmemmb + " MB" + ChatColor.DARK_AQUA + ")" + ChatColor.RESET);
		// hard drive
		File file = new File(".");
		long maxspacegb = file.getTotalSpace() / 1024 / 1024 / 1024;
		long freespacegb = file.getFreeSpace() / 1024 / 1024 / 1024;
		sender.sendMessage(ChatColor.GOLD + "Disk usage: " + ChatColor.RED + df.format(((maxspacegb - freespacegb) * 100) / maxspacegb) + "% " + ChatColor.DARK_AQUA + "(" + ChatColor.DARK_GREEN + (maxspacegb - freespacegb) + "/" + maxspacegb + " GB" + ChatColor.DARK_AQUA + ")" + ChatColor.RESET);
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		return Collections.emptyList();
	}

	@Override
	public int getMinArguments() {
		return 0;
	}

}
