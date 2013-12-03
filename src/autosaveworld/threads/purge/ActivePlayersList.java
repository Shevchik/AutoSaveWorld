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

package autosaveworld.threads.purge;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;

public class ActivePlayersList {

	private HashSet<String> plactivencs = new HashSet<String>();
	private HashSet<String> plactivecs = new HashSet<String>();

	public void gatherActivePlayersList(long awaytime) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		//fill lists
		//due to bukkit fucks up itself when we have two player files with different case (test.dat and Test.dat), i had to write this...
		Server server = Bukkit.getServer();
		Class<?> craftofflineplayer = Bukkit.getOfflinePlayer("fakeautopurgeplayer").getClass();
		Constructor<?> ctor = craftofflineplayer.getDeclaredConstructor(server.getClass(),String.class);
		ctor.setAccessible(true);
		File playersdir = new File(Bukkit.getWorlds().get(0).getWorldFolder(),"players");
        for (String file : playersdir.list()) 
        {
        	if (file.endsWith(".dat")) 
        	{
        		String nickname = file.substring(0, file.length() - 4);
        		OfflinePlayer offplayer = (OfflinePlayer) ctor.newInstance(server,nickname);
    			if (System.currentTimeMillis() - offplayer.getLastPlayed() < awaytime) 
    			{
    				plactivecs.add(offplayer.getName());
    				plactivencs.add(offplayer.getName().toLowerCase());
    			}
        	}
        }
	}

	public int getActivePlayersCount() 
	{
		return plactivecs.size();
	}
	
	public boolean isActiveNCS(String playername)
	{
		return plactivencs.contains(playername.toLowerCase());
	}

	public boolean isActiveCS(String playername)
	{
		return plactivecs.contains(playername);
	}
	
	
}
