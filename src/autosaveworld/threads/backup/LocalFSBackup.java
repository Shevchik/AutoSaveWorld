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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import autosaveworld.config.AutoSaveConfig;
import autosaveworld.core.AutoSaveWorld;

public class LocalFSBackup {
	
    private FileConfiguration configbackup;
    private String datebackup;

    
    private AutoSaveWorld plugin;
    private AutoSaveConfig config;
    public LocalFSBackup(AutoSaveWorld plugin, AutoSaveConfig config)
    {
    	this.plugin = plugin;
    	this.config = config;
    	datebackup = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(plugin.backupThread6.datesec);
    }
    
	
	private int numberofbackupsext = 0;
	private List<Long> backupnamesext;
	private int numberofbackupspl = 0;
	private List<Long> backupnamespl;
	private void loadConfigBackupExt(String extpath){
		configbackup = YamlConfiguration.loadConfiguration(new File(extpath+File.separator+"backups"+File.separator+"backups.yml"));
		numberofbackupsext = configbackup.getInt("worlds.numberofbackups", 0);
		backupnamesext = configbackup.getLongList("worlds.listnames");
		numberofbackupspl = configbackup.getInt("plugins.numberofbackups", 0);
		backupnamespl = configbackup.getLongList("plugins.listnames");
	}
	
	
	private void saveConfigBackupExt(String extpath){
		configbackup = new YamlConfiguration();
		configbackup.set("worlds.numberofbackups", numberofbackupsext);
		configbackup.set("worlds.listnames", backupnamesext);
		configbackup.set("plugins.numberofbackups", numberofbackupspl);
		configbackup.set("plugins.listnames", backupnamespl);
		try {
			configbackup.save(new File(extpath+File.separator+"backups"+File.separator+"backups.yml"));
		} catch (IOException e) {
		}
	}
	
	
	public void performBackup() {

		boolean zip = config.lfsbackupzip;
	    List<String> backupfoldersdest = new ArrayList<String>();

		//adding internal folder to list of folders to which we should backup everything 
		if (!(config.lfsdonotbackuptointfld && config.lfsbackuptoextfolders))  {
			try {
				backupfoldersdest.add(new File(".").getCanonicalPath());
			} catch (IOException e) {e.printStackTrace();}
		}
		//adding external folders to list of folders to which we should backup everything 
		if (config.lfsbackuptoextfolders) {backupfoldersdest.addAll(config.lfsextfolders);}
		
		//backup time	
		for (String extpath : backupfoldersdest)
		{
			//load info about backups stored in file backups.yml
			loadConfigBackupExt(extpath);

			//start worlds backup
			//delete oldest worlds backup if needed
			if (!(config.lfsMaxNumberOfWorldsBackups == 0) && (numberofbackupsext >= config.lfsMaxNumberOfWorldsBackups)) {
				plugin.debug("Deleting oldest worlds backup");
				String pathtoworldsfld = extpath+File.separator+"backups"+File.separator+"worlds";
				//delete worlds oldest backup
				for (String deldir : new File(pathtoworldsfld).list())
				{
					String dirtodelete = pathtoworldsfld+File.separator+deldir+File.separator+datebackup;
					deleteDirectory(new File(dirtodelete)); 
					deleteDirectory(new File(dirtodelete+".zip"));
				}
				backupnamesext.remove(0);
				numberofbackupsext--;
			}
			//do worlds backup
			plugin.debug("Backuping Worlds");
			backupWorlds(config.lfsbackupWorlds, zip, extpath);
			plugin.debug("Backuped Worlds");
			backupnamesext.add(plugin.backupThread6.datesec);
			numberofbackupsext++;
			
			//now do plugins backup
			if (config.lfsbackuppluginsfolder) {
				//remove oldest plugins backup
				if (!(config.lfsMaxNumberOfPluginsBackups == 0) && (numberofbackupspl >= config.lfsMaxNumberOfPluginsBackups)) {
					plugin.debug("Deleting oldest plugins backup");
					String fldtodel = extpath+File.separator+"backups"+File.separator+"plugins"+File.separator+datebackup;
					deleteDirectory(new File(fldtodel));
					deleteDirectory(new File(fldtodel+".zip"));
					backupnamespl.remove(0);
					numberofbackupspl--;
				}	
				//do plugins backup
				plugin.debug("Backuping plugins");
				backupPlugins(zip,extpath);
				plugin.debug("Backuped plugins");
				backupnamespl.add(plugin.backupThread6.datesec);
				numberofbackupspl++;
			}
			
			//save info about backups
			saveConfigBackupExt(extpath);
		}
	}
	
	
	
	//all==true- * in worlds list;
	private void backupWorlds(List<String> worldNames, final boolean zip, final String extpath)
	{
		//create executor
		int maxthreads = Runtime.getRuntime().availableProcessors() - 1;
		if (maxthreads == 0) {maxthreads = 1;}
		ExecutorService worldsbackupService = new ThreadPoolExecutor(maxthreads, maxthreads, 1, TimeUnit.MILLISECONDS,
		    new ArrayBlockingQueue<Runnable>(maxthreads, true),
		    new ThreadPoolExecutor.CallerRunsPolicy()
		);
		
		//now get list of worlds
		boolean all= false;
		if (config.lfsbackupWorlds.contains("*")) {all = true;}
		List<World> worlds = plugin.getServer().getWorlds();
		for (final World world : worlds) {
		if (worldNames.contains(world.getWorldFolder().getName())||all) {
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
									copyDirectory((worldfolder), new File(pathtoworldsb));
								} else { 
									Zip zipfld = new Zip(config.lfsexcludefolders);
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
	
	
	private void backupPlugins(boolean zip, String extpath) {
		try {
			String foldercopyto = extpath+File.separator+"backups"+File.separator+"plugins"+File.separator+datebackup;
			if (!zip) {
				copyDirectory(new File((new File(".").getCanonicalPath())+File.separator+"plugins"),new File(foldercopyto));
			} else  {
				Zip zipfld = new Zip(config.lfsexcludefolders);
				zipfld.ZipFolder(new File((new File(".").getCanonicalPath())+File.separator+"plugins"),new File(foldercopyto+".zip"));
			}
		} catch (IOException e) {e.printStackTrace();}
	}

	
	
	
	private void copyDirectory(File sourceLocation , File targetLocation) throws IOException 
	{
			    if (sourceLocation.isDirectory()) 
			    {
			    	if (!targetLocation.exists()) {
			            targetLocation.mkdirs();
			        }
			        String[] children = sourceLocation.list();
			        for (int i=0; i<children.length; i++) {
			        	boolean copy = true;
		        		//ignore configured folders
			        	for (String efname : config.lfsexcludefolders)
			        	{
			        		if ((new File(sourceLocation, children[i]).getAbsoluteFile()).equals(new File(efname).getAbsoluteFile())) {copy = false; break;}
			        	}
		        		//ignore others worlds folders (mcpc+ only)
			        	for (World w : Bukkit.getWorlds())
			        	{
			        		if (new File(sourceLocation, children[i]).isDirectory() && new File(sourceLocation, children[i]).getName().equals(w.getWorldFolder().getName())) {copy = false; break;}
			        	}
			        	if (copy) {
			        		copyDirectory(new File(sourceLocation, children[i]), new File(targetLocation, children[i])); 
			        	}
			        }
			    } 
			    else 
			    {
			       	if (sourceLocation.canRead()) {
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
			       			plugin.debug("Failed to backup file "+sourceLocation);
			       			e.printStackTrace();
			       		}
			       		try {Thread.sleep(0);} catch (Exception e) {};
			       	}
			    }
	}
			
	private void deleteDirectory(File file)
	{
		if(!file.exists()) {return;}
	    if(file.isDirectory())
	    {
	    	for(File f : file.listFiles())
	    	  deleteDirectory(f);
	    	file.delete();
	    }
	    else
	    {
	    	file.delete();
	    }
	}
	
}
