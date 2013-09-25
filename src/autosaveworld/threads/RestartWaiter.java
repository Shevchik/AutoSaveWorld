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

package autosaveworld.threads;

import java.util.HashSet;

//autorestart shutdown hook uses this class to check if no others shutdown hooks are requiring for restart to wait.
public class RestartWaiter {

	//if this hashset is empty then autorestart shutdown hook will start restarting process.
	//if it is not empty autorestart will wait until it becomes empty
	private volatile HashSet<String> reasons = new HashSet<String>();
	
	public boolean canRestartNow()
	{
		return reasons.isEmpty();
	}
	
	public HashSet<String> getReasons()
	{
		return reasons;
	}
	
	public void addReason(String reason)
	{
		reasons.add(reason);
	}
	
	public void removeReason(String reason)
	{
		reasons.remove(reason);
	}
	
}
