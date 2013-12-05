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

	public boolean isFolderExcluded(List<String> excludelist, String folderPath)
	{
		File folder = new File(folderPath);
		
		//ignore configured folders
		for (String excludedFolderPath : excludelist) 
		{
			if (excludedFolderPath.contains("*")) 
			{
				//resolve wildcard
				//check parents folders equality
				if (folder.getAbsoluteFile().getParentFile().equals(new File(excludedFolderPath).getAbsoluteFile().getParentFile()))
				{
					//check name equality
					String excludeFolderName = new File(excludedFolderPath).getName();
					if (folder.getName().contains(excludeFolderName.substring(0,excludeFolderName.indexOf("*"))))
					{
						return true;
					}
				}
			} else 
			{
				//plain folders equality check
				if (folder.getAbsoluteFile().equals(new File(excludedFolderPath).getAbsoluteFile())) 
				{
					return true;
				}
			}
		}
	
		//ignore others worlds folders inside main world folder (for mcpc+)
		//check if we trying to backup folder inside main world folder
		if (Bukkit.getWorlds().get(0).getWorldFolder().getAbsoluteFile().equals(folder.getParentFile()))
		{
			//if this folder name is equal to one of the world names we should ignore it
			for (World w : Bukkit.getWorlds()) 
			{
				if (folder.getName().equals(w.getWorldFolder().getName())) 
				{
					return true;
				}
			}
		}

    	return false;
	}
	
}
