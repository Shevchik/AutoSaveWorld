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

package autosaveworld.features.purge;

import autosaveworld.config.AutoSaveWorldConfig;

public abstract class DataPurge {

	protected AutoSaveWorldConfig config;
	protected ActivePlayersList activeplayerslist;

	public DataPurge(AutoSaveWorldConfig config, ActivePlayersList activeplayerslist) {
		this.config = config;
		this.activeplayerslist = activeplayerslist;
	}

	public abstract void doPurge();

	private int deleted;
	private int cleaned;

	protected void incDeleted() {
		deleted++;
	}

	protected void incCleaned() {
		cleaned++;
	}

	protected int getDeleted() {
		return deleted;
	}

	protected int getCleaned() {
		return cleaned;
	}

}
