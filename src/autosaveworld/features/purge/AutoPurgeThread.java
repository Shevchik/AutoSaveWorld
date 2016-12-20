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
import autosaveworld.config.AutoSaveWorldConfigMSG;
import autosaveworld.core.logging.MessageLogger;
import autosaveworld.features.IntervalTaskThread;
import autosaveworld.features.purge.plugins.DatfilePurge;
import autosaveworld.features.purge.plugins.essentials.EssentialsPurge;
import autosaveworld.features.purge.plugins.lwc.LWCPurge;
import autosaveworld.features.purge.plugins.mywarp.MyWarpPurge;
import autosaveworld.features.purge.plugins.permissions.PermissionsPurge;
import autosaveworld.features.purge.plugins.wg.WGPurge;

public class AutoPurgeThread extends IntervalTaskThread {

	private AutoSaveWorldConfig config;
	private AutoSaveWorldConfigMSG configmsg;

	public AutoPurgeThread(AutoSaveWorldConfig config, AutoSaveWorldConfigMSG configmsg) {
		super("AutoPurgeThread");
		this.config = config;
		this.configmsg = configmsg;
	}

	@Override
	public boolean isEnabled() {
		return config.purgeEnabled;
	}

	@Override
	public int getInterval() {
		return config.purgeInterval;
	}

	@Override
	public void doTask() {
		performPurge();
	}

	public void performPurge() {
		MessageLogger.broadcast(configmsg.messagePurgeBroadcastPre, config.purgeBroadcast);

		MessageLogger.debug("Purge started");

		MessageLogger.debug("Finiding active players");
		ActivePlayersList activelist = new ActivePlayersList(config);
		activelist.calculateActivePlayers(config.purgeAwayTime * 1000);
		MessageLogger.debug("Found " + activelist.getActivePlayersCount() + " active players");

		ArrayList<DataPurge> purges = new ArrayList<DataPurge>();
		PluginManager pm = Bukkit.getPluginManager();
		if ((pm.getPlugin("WorldGuard") != null) && config.purgeWG) {
			MessageLogger.debug("WG found, adding to purge list");
			purges.add(new WGPurge(config, activelist));
		}
		if ((pm.getPlugin("LWC") != null) && config.purgeLWC) {
			MessageLogger.debug("LWC found, adding to purge list");
			purges.add(new LWCPurge(config, activelist));
		}
		if ((pm.getPlugin("MyWarp") != null) && config.purgeMyWarp) {
			MessageLogger.debug("MyWarp found, adding to purge list");
			purges.add(new MyWarpPurge(config, activelist));
		}
		if ((pm.getPlugin("Essentials") != null && config.purgeEssentials)) {
			MessageLogger.debug("Essentials found, adding to purge list");
			purges.add(new EssentialsPurge(config, activelist));
		}
		if (config.purgePerms) {
			MessageLogger.debug("Permissions purge is enabled, adding to purge list");
			purges.add(new PermissionsPurge(config, activelist));
		}
		if (config.purgeDat) {
			MessageLogger.debug("Dat purge is enabled, adding to purge list");
			purges.add(new DatfilePurge(config, activelist));
		}

		for (DataPurge datapurge : purges) {
			datapurge.doPurge();
		}

		MessageLogger.debug("Purge finished");

		MessageLogger.broadcast(configmsg.messagePurgeBroadcastPost, config.purgeBroadcast);

	}

}
