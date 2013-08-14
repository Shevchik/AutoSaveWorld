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

package autosaveworld.threads.backup;

import java.io.File;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.World;

public class ExcludeManager {

	
	public static boolean isFolderExcluded(List<String> excludelist, String folder)
	{
		//ignore configured fodlers
		for (String ef : excludelist) {
			if (ef.contains("*") && ef.indexOf("*") == ef.length()-1) {
				//resolve wildcard
				//check parents folders equality
				if (new File(folder).getAbsoluteFile().getParentFile().equals(new File(ef).getAbsoluteFile().getParentFile()))
				{
					//check name equality
					String efname = new File(ef).getName();
					if (new File(folder).getName().contains(efname.substring(0,efname.indexOf("*"))))
					{
						return true;
					}
				}
			} else {
				//plain folders equality check
				if (new File(folder).getAbsoluteFile().equals(new File(ef).getAbsoluteFile())) {
					return true;
				}
			}
		}
		
		
		//ignore others worlds folders (for mcpc+)
    	for (World w : Bukkit.getWorlds()) {
    		if (new File(folder).isDirectory() && new File(folder).getName().equals(w.getWorldFolder().getName())) {
    			return true;
    		}
    	}
    	
    	return false;
    	
	}
	
	
	
	
}
