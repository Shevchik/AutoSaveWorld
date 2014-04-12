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

import autosaveworld.config.AutoSaveWorldConfig;
import autosaveworld.core.AutoSaveWorld;
import autosaveworld.threads.purge.byuuids.plugins.DatfilePurge;

public class PurgeByUUIDs {

	private AutoSaveWorld plugin = null;
	private AutoSaveWorldConfig config;
	public PurgeByUUIDs(AutoSaveWorld plugin, AutoSaveWorldConfig config) {
		this.plugin = plugin;
		this.config = config;
	}

	public void startPurge() {
		plugin.debug("Gathering active players list");
		ActivePlayersList aplist = new ActivePlayersList(plugin, config);
		aplist.gatherActivePlayersList(config.purgeAwayTime * 1000);
		plugin.debug("Found "+aplist.getActivePlayersCount()+" active players");

		plugin.debug("Purging player .dat files");
		if (config.purgedat) {
			try {
				new DatfilePurge(plugin).doDelPlayerDatFileTask(aplist);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
