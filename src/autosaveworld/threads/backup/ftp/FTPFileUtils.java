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

package autosaveworld.threads.backup.ftp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.net.ftp.FTPClient;

import autosaveworld.threads.backup.ExcludeManager;

public class FTPFileUtils {

	
    public void uploadDirectoryToFTP(FTPClient ftp, File src, List<String> excludefolders) throws IOException 
    {
    	if (src.isDirectory()) 
    	{
           ftp.makeDirectory(src.getName());
           ftp.changeWorkingDirectory(src.getName());
           for (File file : src.listFiles()) {
        	   	if (!ExcludeManager.isFolderExcluded(excludefolders, file.getPath())) {
        	   		uploadDirectoryToFTP(ftp, file, excludefolders);
        	   	}
           }
           ftp.changeToParentDirectory();
    	}
    	else 
    	{
	    	//ignore lock files
	    	if (!src.getName().endsWith(".lck"))
	    	{
	    		InputStream is = null;
	    		try {
	    			is = src.toURI().toURL().openStream();
	    			ftp.storeFile(src.getName(), is);
	    		}
	    		finally {
	    			is.close();
	    		}
	       		try {Thread.sleep(0);} catch (Exception e) {}
	    	}
       }
   }
	
}
