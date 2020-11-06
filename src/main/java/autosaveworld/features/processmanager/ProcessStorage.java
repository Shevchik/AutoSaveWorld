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

import java.util.HashMap;
import java.util.HashSet;

public class ProcessStorage {

	private HashMap<String, RunningProcess> processes = new HashMap<String, RunningProcess>();

	public void registerProcess(String name, RunningProcess pr) {
		processes.put(name, pr);
	}

	public RunningProcess getProcess(String name) {
		return processes.get(name);
	}

	public void unregisterProcess(String name) {
		processes.remove(name);
	}

	public HashSet<String> getRegisteredProcesses() {
		return new HashSet<String>(processes.keySet());
	}

}
