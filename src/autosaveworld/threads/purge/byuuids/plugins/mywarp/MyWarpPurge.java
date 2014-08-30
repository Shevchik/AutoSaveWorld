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

package autosaveworld.threads.purge.byuuids.plugins.mywarp;

import java.util.TreeSet;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.Warp;

import org.bukkit.plugin.java.JavaPlugin;

import autosaveworld.core.logging.MessageLogger;
import autosaveworld.threads.purge.byuuids.ActivePlayersList;

public class MyWarpPurge {

	public void doMyWarpPurgeTask(ActivePlayersList activePlayersStorage) {

		MessageLogger.debug("MyWarp purge started");

		MyWarp mywarp = JavaPlugin.getPlugin(MyWarp.class);

		int deleted = 0;

		TaskQueue queue = new TaskQueue();
		TreeSet<Warp> warps = mywarp.getWarpManager().getWarps(null, null);
		for (Warp warp : warps) {
			MyWarpInvitesClearTask invitesClearTask = new MyWarpInvitesClearTask(warp);
			if (!warp.isPublicAll()) {
				for (String name : warp.getAllInvitedPlayers()) {
					if (!activePlayersStorage.isActiveName(name)) {
						MessageLogger.debug("Warp member "+name+" is inactive");
						invitesClearTask.add(name);
					}
				}
			}
			// delete warp if owner and members are inactive
			if (!activePlayersStorage.isActiveName(warp.getCreator()) && (invitesClearTask.getPlayerToClearCount() == warp.getAllInvitedPlayers().size())) {
				MessageLogger.debug("Warp owner "+warp.getCreator()+" is inactive");
				// delete warp
				WarpDeleteTask deleteTask = new WarpDeleteTask(warp);
				queue.addTask(deleteTask);

				deleted += 1;
				continue;
			}
			// cleanup invited players if needed
			if (invitesClearTask.hasPlayersToClear()) {
				queue.addTask(invitesClearTask);
			}
		}
		// flush the rest of the batch
		queue.flush();

		MessageLogger.debug("MyWarp purge finished, deleted " + deleted + " inactive warps");
	}

}
