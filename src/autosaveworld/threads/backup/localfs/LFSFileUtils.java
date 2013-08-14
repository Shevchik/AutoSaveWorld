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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import autosaveworld.threads.backup.ExcludeManager;

public class LFSFileUtils {

	public void copyDirectory(File sourceLocation , File targetLocation, List<String> excludefolders) throws IOException 
	{
			    if (sourceLocation.isDirectory()) 
			    {
			    	if (!targetLocation.exists()) 
			    	{
			            targetLocation.mkdirs();
			        }
			        String[] children = sourceLocation.list();
			        for (int i=0; i<children.length; i++) 
			        {
			        	if (!ExcludeManager.isFolderExcluded(excludefolders, new File(sourceLocation, children[i]).getPath())) {
			        		copyDirectory(new File(sourceLocation, children[i]), new File(targetLocation, children[i]), excludefolders); 
			        	}
			        }
			    } 
			    else 
			    {
			    	//ignore lock files
			    	if (!sourceLocation.getName().endsWith(".lck"))
			    	{
					    try {
					    	InputStream in = new FileInputStream(sourceLocation);
			       			OutputStream out = new FileOutputStream(targetLocation);
			       			byte[] buf = new byte[4096];
			       			int len;
			       			while ((len = in.read(buf)) > 0) {
			       				out.write(buf, 0, len);
			       			}
			       			in.close();
			       			out.close();
			    		} catch (Exception e) {
			    			
			       		}
			       		try {Thread.sleep(0);} catch (Exception e) {e.printStackTrace();};
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
	
	
}
