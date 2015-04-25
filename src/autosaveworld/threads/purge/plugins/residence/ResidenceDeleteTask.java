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

package autosaveworld.threads.purge.plugins.residence;

import net.t00thpick1.residence.api.ResidenceAPI;
import net.t00thpick1.residence.api.areas.ResidenceArea;
import autosaveworld.core.logging.MessageLogger;
import autosaveworld.threads.purge.taskqueue.Task;

public class ResidenceDeleteTask implements Task {

	private ResidenceArea resarea;

	public ResidenceDeleteTask(ResidenceArea resarea) {
		this.resarea = resarea;
	}

	@Override
	public boolean isHeavyTask() {
		return false;
	}

	@Override
	public void performTask() {
		MessageLogger.debug("Deleting residence " + resarea.getName());
		ResidenceAPI.getResidenceManager().remove(resarea);
	}

}
