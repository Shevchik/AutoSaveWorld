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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Queue;

import org.bukkit.command.CommandSender;

public class RunningProcess {

	private String[] args;

	public RunningProcess(String[] args) {
		this.args = args.clone();
	}

	protected Process p;
	protected Queue<String> output = new LinkedList<String>();

	public void start(CommandSender sender) {
		sender.sendMessage("Starting process");
		ProcessBuilder pb = new ProcessBuilder();
		pb.redirectErrorStream(true);
		pb.command(args);
		try {
			p = pb.start();
		} catch (Exception e) {
			sender.sendMessage("Error occured while starting process");
			sender.sendMessage(e.getMessage());
			return;
		}
		new Thread() {
			@Override
			public void run() {
				try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
					String line;
					while ((p != null) && ((line = br.readLine()) != null)) {
						output.add(line);
					}
				} catch (IOException e) {
				}
			}
		}.start();
		sender.sendMessage("Process started");
	}

	public void printOutput(CommandSender sender) {
		sender.sendMessage("Printing latest process output");
		String line;
		while ((line = output.poll()) != null) {
			sender.sendMessage(line);
		}
		try {
			int exit = p.exitValue();
			sender.sendMessage("Process finished exit code " + exit);
		} catch (IllegalThreadStateException e) {
		}
		sender.sendMessage("Process output print finished");
	}

	public void supplyInput(CommandSender sender, String line) {
		sender.sendMessage("Sending line to the process");
		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(p.getOutputStream(), StandardCharsets.UTF_8))) {
			writer.write(line);
			writer.newLine();
			writer.flush();
		} catch (IOException e) {
			sender.sendMessage("Error occured while sending line to process");
			sender.sendMessage(e.getMessage());
			return;
		}
		sender.sendMessage("Line sent");
	}

	public void stop(CommandSender sender) {
		sender.sendMessage("Stopping process");
		p.destroy();
		p = null;
		sender.sendMessage("Process stopped");
	}

}
