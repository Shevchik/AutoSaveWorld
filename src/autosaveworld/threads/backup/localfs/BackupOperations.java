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
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.World;

import autosaveworld.core.AutoSaveWorld;
import autosaveworld.threads.backup.Zip;

public class BackupOperations {

	AutoSaveWorld plugin;
	final boolean zip;
	final String extpath;
	final List<String> excludefolders;
    private String datebackup;
    private FileUtils fu;
	public BackupOperations(AutoSaveWorld plugin, boolean zip, String extpath, List<String> excludefolders)
	{
		this.plugin = plugin;
		this.zip = zip;
		this.extpath = extpath;
		this.excludefolders = excludefolders;
		datebackup = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(plugin.backupThread6.datesec);
		fu = new FileUtils();
	}
	
	//worlds backup operations
	
	public void backupWorlds(List<String> worldNames)
	{
		//create executor
		int maxthreads = Runtime.getRuntime().availableProcessors() - 1;
		if (maxthreads == 0) {maxthreads = 1;}
		ExecutorService worldsbackupService = new ThreadPoolExecutor(maxthreads, maxthreads, 1, TimeUnit.MILLISECONDS,
		    new ArrayBlockingQueue<Runnable>(maxthreads, true),
		    new ThreadPoolExecutor.CallerRunsPolicy()
		);
		
		//now get list of worlds
		List<World> worlds = Bukkit.getWorlds();
		for (final World world : worlds) {
			if (worldNames.contains(world.getWorldFolder().getName())) {
				//create runnable for ThreadPoolExecutor
				Runnable worldb = new Runnable()
				{
						public void run()
						{
							plugin.debug("Backuping world "+world.getWorldFolder().getName());

							world.setAutoSave(false);
							try {
								File worldfolder = world.getWorldFolder().getCanonicalFile();
								String pathtoworldsb = extpath+File.separator+"backups"+File.separator+"worlds"+File.separator+world.getWorldFolder().getName()+File.separator+datebackup;
								if (!zip) {
									fu.copyDirectory((worldfolder), new File(pathtoworldsb),excludefolders);
								} else { 
									Zip zipfld = new Zip(excludefolders);
									zipfld.ZipFolder((worldfolder), new File(pathtoworldsb+".zip"));
								}
							} catch (Exception e) {
								plugin.debug("Failed to backup world "+world.getWorldFolder().getName());
								e.printStackTrace();
							} finally {
								world.setAutoSave(true); 
							}
							plugin.debug("Backuped world "+world.getWorldFolder().getName());
						}
				};
				//Add task to executor
				worldsbackupService.submit(worldb);
			}
		}
		//wait for executor to finish (let's hope that the backup will finish in max 2 days)
		worldsbackupService.shutdown();
		try {
			worldsbackupService.awaitTermination(48, TimeUnit.HOURS);
		} catch (InterruptedException e) {e.printStackTrace();}
	}
	
	public void deleteOldestWorldBackup(String oldestbackupdate)
	{
			String pathtoworldsfld = extpath+File.separator+"backups"+File.separator+"worlds";
			//delete worlds oldest backup
			for (String deldir : new File(pathtoworldsfld).list())
			{
				String dirtodelete = pathtoworldsfld+File.separator+deldir+File.separator+oldestbackupdate;
				fu.deleteDirectory(new File(dirtodelete)); 
				fu.deleteDirectory(new File(dirtodelete+".zip"));
			}
	}	
	
	
	//plugins backup operations
	
	public void backupPlugins() 
	{
		try {
			String foldercopyto = extpath+File.separator+"backups"+File.separator+"plugins"+File.separator+datebackup;
			if (!zip) {
				fu.copyDirectory(new File((new File(".").getCanonicalPath())+File.separator+"plugins"),new File(foldercopyto),excludefolders);
			} else  {
				Zip zipfld = new Zip(excludefolders);
				zipfld.ZipFolder(new File((new File(".").getCanonicalPath())+File.separator+"plugins"),new File(foldercopyto+".zip"));
			}
		} catch (IOException e) {e.printStackTrace();}
	}
	
	public void deleteOldestPluginsBackup(String oldestbackupdate)
	{
		String fldtodel = extpath+File.separator+"backups"+File.separator+"plugins"+File.separator+oldestbackupdate;
		fu.deleteDirectory(new File(fldtodel));
		fu.deleteDirectory(new File(fldtodel+".zip"));
	}
	
}
