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

package autosaveworld.threads.purge.plugins.lwc;

import org.bukkit.Material;

import autosaveworld.threads.purge.taskqueue.Task;

import com.griefcraft.model.Protection;

public class LWCRegenTask implements Task {

	private Protection protection;

	public LWCRegenTask(Protection protection) {
		this.protection = protection;
	}

	@Override
	public boolean isHeavyTask() {
		return false;
	}

	@Override
	public void performTask() {
		protection.getBlock().setType(Material.AIR);
	}

}
