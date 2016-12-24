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

package autosaveworld.features.purge;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

import autosaveworld.config.AutoSaveWorldConfig;
import autosaveworld.core.AutoSaveWorld;
import autosaveworld.core.logging.MessageLogger;
import autosaveworld.features.purge.plugins.DatfilePurge;
import autosaveworld.features.purge.plugins.essentials.EssentialsPurge;
import autosaveworld.features.purge.plugins.lwc.LWCPurge;
import autosaveworld.features.purge.plugins.mywarp.MyWarpPurge;
import autosaveworld.features.purge.plugins.permissions.PermissionsPurge;
import autosaveworld.features.purge.plugins.wg.WGPurge;
import autosaveworld.utils.Threads.IntervalTaskThread;

public class AutoPurgeThread extends IntervalTaskThread {

	public AutoPurgeThread() {
		super("AutoPurgeThread");
	}

	@Override
	public boolean isEnabled() {
		return AutoSaveWorld.getInstance().getMainConfig().purgeEnabled;
	}

	@Override
	public int getInterval() {
		return AutoSaveWorld.getInstance().getMainConfig().purgeInterval;
	}

	@Override
	public void doTask() {
		performPurge();
	}

	public void performPurge() {
		AutoSaveWorldConfig config = AutoSaveWorld.getInstance().getMainConfig();

		MessageLogger.broadcast(AutoSaveWorld.getInstance().getMessageConfig().messagePurgeBroadcastPre, config.purgeBroadcast);

		MessageLogger.debug("Purge started");

		MessageLogger.debug("Finiding active players");
		ActivePlayersList activelist = new ActivePlayersList(config.purgeIgnoredNicks, config.purgeIgnoredUUIDs);
		activelist.calculateActivePlayers(AutoSaveWorld.getInstance().getMainConfig().purgeAwayTime * 1000);
		MessageLogger.debug("Found " + activelist.getActivePlayersCount() + " active players");

		ArrayList<DataPurge> purges = new ArrayList<DataPurge>();
		PluginManager pm = Bukkit.getPluginManager();
		if ((pm.getPlugin("WorldGuard") != null) && config.purgeWG) {
			MessageLogger.debug("WG found, adding to purge list");
			purges.add(new WGPurge(activelist));
		}
		if ((pm.getPlugin("LWC") != null) && config.purgeLWC) {
			MessageLogger.debug("LWC found, adding to purge list");
			purges.add(new LWCPurge(activelist));
		}
		if ((pm.getPlugin("MyWarp") != null) && config.purgeMyWarp) {
			MessageLogger.debug("MyWarp found, adding to purge list");
			purges.add(new MyWarpPurge(activelist));
		}
		if ((pm.getPlugin("Essentials") != null && config.purgeEssentials)) {
			MessageLogger.debug("Essentials found, adding to purge list");
			purges.add(new EssentialsPurge(activelist));
		}
		if (config.purgePerms) {
			MessageLogger.debug("Permissions purge is enabled, adding to purge list");
			purges.add(new PermissionsPurge(activelist));
		}
		if (config.purgeDat) {
			MessageLogger.debug("Dat purge is enabled, adding to purge list");
			purges.add(new DatfilePurge(activelist));
		}

		for (DataPurge datapurge : purges) {
			datapurge.doPurge();
		}

		MessageLogger.debug("Purge finished");

		MessageLogger.broadcast(AutoSaveWorld.getInstance().getMessageConfig().messagePurgeBroadcastPost, config.purgeBroadcast);
	}

}
