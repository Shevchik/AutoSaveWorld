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

package autosaveworld.threads.purge.plugins;

import java.util.ArrayList;
import java.util.TreeSet;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.Warp;

import org.bukkit.Bukkit;

import autosaveworld.core.AutoSaveWorld;
import autosaveworld.threads.purge.ActivePlayersList;

public class MyWarpPurge {

	private AutoSaveWorld plugin;

	public MyWarpPurge(AutoSaveWorld plugin) {
		this.plugin = plugin;
	}

	public void doMyWarpPurgeTask(ActivePlayersList pacheck) {

		plugin.debug("MyWarp purge started");

		MyWarp mywarp = (MyWarp) Bukkit.getPluginManager().getPlugin("MyWarp");

		int deleted = 0;

		TreeSet<Warp> warps = mywarp.getWarpManager().getWarps(null, null);
		for (Warp warp : warps)
		{
			if (!pacheck.isActiveCS(warp.getCreator()))
			{
				//add warp to delete batch
				warptodel.add(warp);
				//delete warps if maximum batch size reached
				if (warptodel.size() == 80)
				{
					flushBatch(mywarp);
				}
				//count deleted protections
				deleted += 1;
			}
		}
		//flush the rest of the batch
		flushBatch(mywarp);

		plugin.debug("MyWarp purge finished, deleted "+ deleted+" inactive warps");
	}

	private ArrayList<Warp> warptodel = new ArrayList<Warp>(100);
	private void flushBatch(final MyWarp mywarp)
	{
		Runnable rempr = new Runnable()
		{
			@Override
			public void run()
			{
				for (Warp warp : warptodel)
				{
					//delete warp
					plugin.debug("Removing warp for inactive player "+warp.getCreator());
					mywarp.getWarpManager().deleteWarp(warp);
				}
				warptodel.clear();
			}
		};
		int taskid = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, rempr);

		while (Bukkit.getScheduler().isCurrentlyRunning(taskid) || Bukkit.getScheduler().isQueued(taskid))
		{
			try {Thread.sleep(50);} catch (InterruptedException e) {}
		}
	}

}
