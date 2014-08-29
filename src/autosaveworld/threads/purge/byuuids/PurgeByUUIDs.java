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

package autosaveworld.threads.purge.byuuids;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

import autosaveworld.config.AutoSaveWorldConfig;
import autosaveworld.core.AutoSaveWorld;
import autosaveworld.core.logging.MessageLogger;
import autosaveworld.threads.purge.byuuids.plugins.DatfilePurge;
import autosaveworld.threads.purge.byuuids.plugins.LWCPurge;
import autosaveworld.threads.purge.byuuids.plugins.MVInvPurge;
import autosaveworld.threads.purge.byuuids.plugins.MyWarpPurge;
import autosaveworld.threads.purge.byuuids.plugins.permissions.PermissionsPurge;
import autosaveworld.threads.purge.byuuids.plugins.residence.ResidencePurge;
import autosaveworld.threads.purge.byuuids.plugins.wg.WGPurge;

public class PurgeByUUIDs {

	private AutoSaveWorldConfig config;

	public PurgeByUUIDs(AutoSaveWorld plugin, AutoSaveWorldConfig config) {
		this.config = config;
	}

	public void startPurge() {
		MessageLogger.debug("Gathering active players list");
		ActivePlayersList aplist = new ActivePlayersList(config);
		aplist.gatherActivePlayersList(config.purgeAwayTime * 1000);
		MessageLogger.debug("Found " + aplist.getActivePlayersCount() + " active players");

		PluginManager pm = Bukkit.getPluginManager();

		if ((pm.getPlugin("WorldGuard") != null) && config.purgeWG) {
			MessageLogger.debug("WG found, purging");
			try {
				new WGPurge().doWGPurgeTask(aplist, config.purgeWGRegenRg, config.purgeWGNoregenOverlap);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}

		if ((pm.getPlugin("LWC") != null) && config.purgeLWC) {
			MessageLogger.debug("LWC found, purging");
			try {
				new LWCPurge().doLWCPurgeTask(aplist, config.purgeLWCDelProtectedBlocks);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}

		if ((pm.getPlugin("Multiverse-Inventories") != null) && config.purgeMVInv) {
			MessageLogger.debug("Multiverse-Inventories found, purging");
			try {
				new MVInvPurge().doMVInvPurgeTask(aplist);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}

		if ((pm.getPlugin("Residence") != null) && config.purgeResidence) {
			MessageLogger.debug("Residence found, purging");
			try {
				new ResidencePurge().doResidencePurgeTask(aplist, config.purgeResidenceRegenArea);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}

		if ((pm.getPlugin("MyWarp") != null) && config.purgeMyWarp) {
			MessageLogger.debug("MyWarp found, purging");
			try {
				new MyWarpPurge().doMyWarpPurgeTask(aplist);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}

		if (config.purgePerms) {
			try {
				new PermissionsPurge().doPermissionsPurgeTask(aplist);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}

		MessageLogger.debug("Purging player .dat files");
		if (config.purgeDat) {
			try {
				new DatfilePurge().doDelPlayerDatFileTask(aplist);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

}
