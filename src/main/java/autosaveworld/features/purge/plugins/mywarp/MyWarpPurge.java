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

import java.util.Collection;
import java.util.UUID;

import org.bukkit.plugin.java.JavaPlugin;

import io.github.mywarp.mywarp.MyWarp;
import io.github.mywarp.mywarp.bukkit.MyWarpPlugin;
import io.github.mywarp.mywarp.util.playermatcher.GroupPlayerMatcher;
import io.github.mywarp.mywarp.util.playermatcher.PlayerMatcher;
import io.github.mywarp.mywarp.util.playermatcher.UuidPlayerMatcher;
import io.github.mywarp.mywarp.warp.Warp;

import autosaveworld.core.logging.MessageLogger;
import autosaveworld.features.purge.ActivePlayersList;
import autosaveworld.features.purge.DataPurge;
import autosaveworld.features.purge.taskqueue.TaskExecutor;
import autosaveworld.utils.ReflectionUtils;

public class MyWarpPurge extends DataPurge {

	public MyWarpPurge(ActivePlayersList activeplayerslist) {
		super("MyWarp", activeplayerslist);
	}

	@Override
	public void doPurge() {
		MyWarpPlugin mywarpplugin = JavaPlugin.getPlugin(MyWarpPlugin.class);
		MyWarp mywarp;
		try {
			mywarp = (MyWarp) ReflectionUtils.getField(mywarpplugin.getClass(), "myWarp").get(mywarpplugin);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}

		try (TaskExecutor queue = new TaskExecutor(80)) {
			Collection<Warp> warps = mywarp.getWarpManager().getAll(warp -> true);
			for (Warp warp : warps) {
				MyWarpInvitesClearTask invitesClearTask = new MyWarpInvitesClearTask(warp);
				for (PlayerMatcher invitation : warp.getInvitations()) {
					if (invitation instanceof GroupPlayerMatcher) {
						continue;
					}
					UUID uuid = ((UuidPlayerMatcher) invitation).getCriteria();
					if (!activeplayerslist.isActiveUUID(uuid)) {
						MessageLogger.debug("Warp member "+uuid+" is inactive");
						invitesClearTask.add(invitation);
					}
				}
				// delete warp if owner and members are inactive
				if (!activeplayerslist.isActiveUUID(warp.getCreator()) && (invitesClearTask.getPlayerToClearCount() == warp.getInvitations().size())) {
					MessageLogger.debug("Warp owner "+warp.getCreator()+" is inactive");
					// delete warp
					MyWapWarpDeleteTask deleteTask = new MyWapWarpDeleteTask(mywarp.getWarpManager(), warp);
					queue.execute(deleteTask);
					incDeleted();
					continue;
				}
				// cleanup invited players if needed
				if (invitesClearTask.hasPlayersToClear()) {
					queue.execute(invitesClearTask);
					incCleaned();
				}
			}
		}
	}

}
