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

package autosaveworld.threads.backup.localfs;

import java.io.File;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.List;

import autosaveworld.threads.backup.ExcludeManager;

public class LFSFileUtils {

	private ExcludeManager eManager = new ExcludeManager();

	public void copyDirectory(File sourceLocation , File targetLocation, List<String> excludefolders)
	{
		if (sourceLocation.isDirectory())
		{
			if (!targetLocation.exists())
			{
				targetLocation.mkdirs();
			}
			for (String filename : sourceLocation.list())
			{
				if (!eManager.isFolderExcluded(excludefolders, new File(sourceLocation, filename).getPath()))
				{
					copyDirectory(new File(sourceLocation, filename), new File(targetLocation, filename), excludefolders);
				}
			}
		} else
		{
			//ignore lock files
			if (!sourceLocation.getName().endsWith(".lck"))
			{
				try {
					Files.copy(sourceLocation.toPath(), targetLocation.toPath());
				} catch (Exception e) {
					e.printStackTrace();
				}
				Thread.yield();
			}
		}
	}

	public void deleteDirectory(File file)
	{
		if(!file.exists()) {return;}
		if(file.isDirectory())
		{
			for(File f : file.listFiles())
			{
				deleteDirectory(f);
			}
			file.delete();
		}
		else
		{
			file.delete();
		}
	}


	public String findOldestBackupName(String backupsfodler)
	{
		String[] timestamps = new File(backupsfodler).list();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
		String oldestBackupName = timestamps[0];
		long old = System.currentTimeMillis();
		try {
			if (oldestBackupName.endsWith(".zip"))
			{
				old = sdf.parse(oldestBackupName.substring(0, oldestBackupName.indexOf(".zip"))).getTime();
			} else
			{
				old = sdf.parse(oldestBackupName).getTime();
			}
			for (String timestampString : timestamps)
			{
				long cur = System.currentTimeMillis();
				if (timestampString.endsWith(".zip"))
				{
					cur = sdf.parse(timestampString.substring(0,timestampString.indexOf(".zip"))).getTime();
				} else
				{
					cur = sdf.parse(timestampString).getTime();
				}
				if (cur < old)
				{
					old = cur;
					oldestBackupName = timestampString;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return oldestBackupName;
	}


}
