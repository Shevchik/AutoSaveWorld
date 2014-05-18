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

package autosaveworld.threads.worldregen;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;

public class PhysicsListener implements Listener {

	@EventHandler
	public void onPhysics(BlockPhysicsEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void onFromTo(BlockFromToEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void onFire(BlockIgniteEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void onPistonExtend(BlockPistonExtendEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void onPistonRetract(BlockPistonRetractEvent event) {
		event.setCancelled(true);	
	}

}
