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

import java.text.MessageFormat;
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
		MessageLogger.debug(MessageFormat.format("Found {0} active players", activelist.getActivePlayersCount()));

		ArrayList<DataPurge> purges = new ArrayList<DataPurge>();
		PluginManager pm = Bukkit.getPluginManager();
		if ((pm.getPlugin("WorldGuard") != null) && config.purgeWG) {
			purges.add(new WGPurge(activelist));
		}
		if ((pm.getPlugin("LWC") != null) && config.purgeLWC) {
			purges.add(new LWCPurge(activelist));
		}
		if ((pm.getPlugin("MyWarp") != null) && config.purgeMyWarp) {
			purges.add(new MyWarpPurge(activelist));
		}
		if ((pm.getPlugin("Essentials") != null && config.purgeEssentials)) {
			purges.add(new EssentialsPurge(activelist));
		}
		if (config.purgePerms) {
			DataPurge permspurge = PermissionsPurge.selectDataPurge(activelist);
			if (permspurge != null) {
				purges.add(permspurge);
			}
		}
		if (config.purgeDat) {
			purges.add(new DatfilePurge(activelist));
		}

		for (DataPurge datapurge : purges) {
			MessageLogger.debug(MessageFormat.format("Started {0} purge", datapurge.getName()));
			try {
				datapurge.doPurge();
				MessageLogger.debug(MessageFormat.format("Finished {0} purge. Removed {1} entries, cleaned {2} entries", datapurge.getName(), datapurge.getDeleted(), datapurge.getCleaned()));
			} catch (Throwable t) {
				MessageLogger.exception(MessageFormat.format("Failed {0} purge", datapurge.getName()), t);
			}
		}

		MessageLogger.debug("Purge finished");

		MessageLogger.broadcast(AutoSaveWorld.getInstance().getMessageConfig().messagePurgeBroadcastPost, config.purgeBroadcast);
	}

}
