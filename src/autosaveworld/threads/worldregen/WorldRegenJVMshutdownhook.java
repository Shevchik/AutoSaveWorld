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

import java.io.File;
import java.io.IOException;

import autosaveworld.threads.restart.RestartJVMshutdownhook;

public class WorldRegenJVMshutdownhook extends Thread {

	private String shouldpastefile;
	private String fldtodelete;
	private RestartJVMshutdownhook jvmsh;

	public WorldRegenJVMshutdownhook(RestartJVMshutdownhook jvmsh, String fldtodelete, String shouldpastefile)
	{
		this.fldtodelete = fldtodelete;
		this.shouldpastefile = shouldpastefile;
		this.jvmsh = jvmsh;
	}

	@Override
	public void run()
	{
		Thread.currentThread().setName("AutoSaveWorld WorldRegenShutdownHook");
		//Delete region from world folder
		deleteDirectory(new File(fldtodelete+File.separator+"region"));
		try {
			//create file that indicates that AutoSaveWorld should paste regions from schematics back to map
			new File(shouldpastefile).createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//restart
		jvmsh.restart();
	}



	private void deleteDirectory(File file)
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


}
