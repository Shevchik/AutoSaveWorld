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

import java.util.List;

import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.base.Predicate;

import autosaveworld.core.logging.MessageLogger;
import autosaveworld.features.purge.ActivePlayersList;
import autosaveworld.features.purge.DataPurge;
import autosaveworld.features.purge.taskqueue.TaskExecutor;
import autosaveworld.utils.ReflectionUtils;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.bukkit.MyWarpPlugin;
import me.taylorkelly.mywarp.util.profile.Profile;
import me.taylorkelly.mywarp.warp.Warp;

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
			List<Warp> warps = mywarp.getWarpManager().getMatchingWarps("", new Predicate<Warp>() {
				@Override
				public boolean apply(Warp warp) {
					return true;
				}
			}).getMatches();
			for (Warp warp : warps) {
				MyWarpInvitesClearTask invitesClearTask = new MyWarpInvitesClearTask(warp);
				for (Profile profile : warp.getInvitedPlayers()) {
					if (!activeplayerslist.isActiveUUID(profile.getUniqueId())) {
						MessageLogger.debug("Warp member "+profile.getUniqueId()+" is inactive");
						invitesClearTask.add(profile);
					}
				}
				// delete warp if owner and members are inactive
				if (!activeplayerslist.isActiveUUID(warp.getCreator().getUniqueId()) && (invitesClearTask.getPlayerToClearCount() == warp.getInvitedPlayers().size())) {
					MessageLogger.debug("Warp owner "+warp.getCreator().getUniqueId()+" is inactive");
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
