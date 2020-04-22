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

package autosaveworld.features.processmanager;

import org.bukkit.command.CommandSender;

import autosaveworld.core.logging.MessageLogger;
import autosaveworld.utils.StringUtils;

public class ProcessManager {

	public void handleProcessManagerCommand(CommandSender sender, String command, String processname, String[] args) {
		if (command.equalsIgnoreCase("start")) {
			runProcess(sender, processname, args);
		} else if (command.equalsIgnoreCase("stop")) {
			killProcess(sender, processname);
		} else if (command.equalsIgnoreCase("output")) {
			printProcessOutput(sender, processname);
		} else if (command.equalsIgnoreCase("input")) {
			supplyProcessInput(sender, processname, StringUtils.join(args, " "));
		} else if (command.equalsIgnoreCase("list")) {
			listProcesses(sender);
		} else {
			MessageLogger.sendMessage(sender, "Invalid process manager command");
		}
	}

	private ProcessStorage storage = new ProcessStorage();

	private void runProcess(CommandSender sender, String prname, String[] args) {
		if (prname == null) {
			sender.sendMessage("Process name is empty");
		}
		if (storage.getProcess(prname) != null) {
			sender.sendMessage("Process with this name already registered");
			return;
		}
		if (args == null) {
			sender.sendMessage("No process args detected");
			return;
		}
		RunningProcess process = new RunningProcess(args);
		storage.registerProcess(prname, process);
		process.start(sender);
	}

	private void printProcessOutput(CommandSender sender, String processname) {
		RunningProcess process = storage.getProcess(processname);
		if (process != null) {
			process.printOutput(sender);
		} else {
			sender.sendMessage("Process with this name is not found");
		}
	}

	private void supplyProcessInput(CommandSender sender, String processname, String line) {
		if (processname == null) {
			sender.sendMessage("Process name is empty");
		}
		RunningProcess process = storage.getProcess(processname);
		if (process != null) {
			process.supplyInput(sender, line);
		} else {
			sender.sendMessage("Process with this name is not found");
		}
	}

	private void killProcess(CommandSender sender, String processname) {
		if (processname == null) {
			sender.sendMessage("Process name is empty");
		}
		RunningProcess process = storage.getProcess(processname);
		if (process != null) {
			storage.unregisterProcess(processname);
			process.stop(sender);
		} else {
			sender.sendMessage("Process with this name is not found");
		}
	}

	private void listProcesses(CommandSender sender) {
		sender.sendMessage("Registered processes:");
		for (String processname : storage.getRegisteredProcesses()) {
			sender.sendMessage(processname);
		}
	}

}
