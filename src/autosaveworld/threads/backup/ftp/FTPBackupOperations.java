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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.net.ftp.FTPClient;
import org.bukkit.Bukkit;
import org.bukkit.World;

import autosaveworld.core.AutoSaveWorld;
import autosaveworld.threads.backup.Zip;
import autosaveworld.threads.backup.ftp.FTPFileUtils;

public class FTPBackupOperations {

	
	AutoSaveWorld plugin;
	final boolean zip;
	final List<String> excludefolders;
    private FTPFileUtils fu;
    private FTPClient ftp;
	public FTPBackupOperations(AutoSaveWorld plugin,FTPClient ftp, boolean zip,  List<String> excludefolders)
	{
		this.plugin = plugin;
		this.zip = zip;
		this.excludefolders = excludefolders;
		fu = new FTPFileUtils();
		this.ftp = ftp;
		localtempfolder = plugin.constants.getBackupTempFolder();
	}
	
	private String localtempfolder;
	public void backupWorlds(List<String> worldNames)
	{
		List<World> worlds = Bukkit.getWorlds();
		for (final World world : worlds) {
			if (worldNames.contains(world.getWorldFolder().getName())) {
				plugin.debug("Backuping world "+world.getWorldFolder().getName());
				
				world.setAutoSave(false);
				try {
					File worldfolder = world.getWorldFolder().getCanonicalFile();
					if (!zip) {
						fu.uploadDirectoryToFTP(ftp, worldfolder, excludefolders);
					} else {
						File tempzip = new File(localtempfolder,worldfolder.getName()+".zip");
						Zip zipfld = new Zip(excludefolders);
						zipfld.ZipFolder(worldfolder, tempzip);
						fu.uploadDirectoryToFTP(ftp, tempzip, new ArrayList<String>());
						tempzip.delete();
						new File(localtempfolder).delete();
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					world.setAutoSave(true);
				}
				
				plugin.debug("Backuped world "+world.getWorldFolder().getName());
			}
		}
	}
	
	
	public void backupPlugins()
	{
		try {
			File plfolder = new File("plugins").getCanonicalFile();
			if (!zip) {
				fu.uploadDirectoryToFTP(ftp, plfolder, excludefolders);
			} else  {
				File tempzip = new File(localtempfolder,plfolder.getName()+".zip");
				Zip zipfld = new Zip(excludefolders);
				zipfld.ZipFolder(plfolder, tempzip);
				fu.uploadDirectoryToFTP(ftp, tempzip, new ArrayList<String>());
				tempzip.delete();
				new File(localtempfolder).delete();
			}
		} catch (IOException e) {e.printStackTrace();}
	}
	
}
