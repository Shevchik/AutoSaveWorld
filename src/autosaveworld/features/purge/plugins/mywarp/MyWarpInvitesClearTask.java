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

import autosaveworld.core.logging.MessageLogger;
import autosaveworld.features.purge.taskqueue.Task;
import me.taylorkelly.mywarp.util.profile.Profile;
import me.taylorkelly.mywarp.warp.Warp;

public class MyWarpInvitesClearTask implements Task {

	private Warp warp;

	public MyWarpInvitesClearTask(Warp warp) {
		this.warp = warp;
	}

	private LinkedList<Profile> profiles = new LinkedList<Profile>();

	public void add(Profile profile) {
		profiles.add(profile);
	}

	public boolean hasPlayersToClear() {
		return !profiles.isEmpty();
	}

	public int getPlayerToClearCount() {
		return profiles.size();
	}

	@Override
	public boolean doNotQueue() {
		return false;
	}

	@Override
	public void performTask() {
		MessageLogger.debug("Cleaning invites for warp "+warp.getName());
		for (Profile name : profiles) {
			warp.uninvitePlayer(name);
		}
	}

}
