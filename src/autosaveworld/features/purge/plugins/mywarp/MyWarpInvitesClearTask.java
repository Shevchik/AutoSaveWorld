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

package autosaveworld.features.purge.plugins.mywarp;

import java.util.LinkedList;

import me.taylorkelly.mywarp.data.Warp;
import autosaveworld.core.logging.MessageLogger;
import autosaveworld.features.purge.taskqueue.Task;

public class MyWarpInvitesClearTask implements Task {

	private Warp warp;

	public MyWarpInvitesClearTask(Warp warp) {
		this.warp = warp;
	}

	private LinkedList<String> names = new LinkedList<String>();

	public void add(String name) {
		names.add(name);
	}

	public boolean hasPlayersToClear() {
		return !names.isEmpty();
	}

	public int getPlayerToClearCount() {
		return names.size();
	}

	@Override
	public boolean isHeavyTask() {
		return false;
	}

	@Override
	public void performTask() {
		MessageLogger.debug("Cleaning invites for warp "+warp.getName());
		for (String name : names) {
			warp.uninvite(name);
		}
	}

}
