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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import autosaveworld.config.AutoSaveConfig;
import autosaveworld.config.AutoSaveConfigMSG;
import autosaveworld.core.AutoSaveWorld;
import autosaveworld.core.Generic;

public class AutoBackupThread extends Thread {

	protected final Logger log = Bukkit.getLogger();
	private boolean run = true;
	private AutoSaveWorld plugin = null;
	private AutoSaveConfig config;
	private AutoSaveConfigMSG configmsg;
    public long datesec;
    private int i;
    private boolean command = false;
    private FileConfiguration configbackup;
	
	// Constructor to define number of seconds to sleep
	public AutoBackupThread(AutoSaveWorld plugin, AutoSaveConfig config, AutoSaveConfigMSG configmsg) {
		this.plugin = plugin;
		this.config = config;
		this.configmsg = configmsg;
	}
	

    
	// Allows for the thread to naturally exit if value is false
	public void setRun(boolean run) {
		this.run = run;
 	}
	
	public void startbackup()
	{
	command = true;
	i = config.backupInterval;
	}
    
	
	// The code to run...weee
	public void run() {

		log.info(String.format("[%s] AutoBackupThread Started: Interval is %d seconds",
						plugin.getDescription().getName(), config.backupInterval
					)
				);
		Thread.currentThread().setName("AutoSaveWorld_AutoBackupThread");
		
		while (run) {
			// Prevent AutoBackup from never sleeping
			// If interval is 0, sleep for 10 seconds and skip backup
			if(config.backupInterval == 0) {
				try {
					Thread.sleep(10000);
				} catch(InterruptedException e) {}
				continue;
			}
			
			// Do our Sleep stuff!
			for (i = 0; i < config.backupInterval; i++) {
				try {
										
					boolean warn = config.backupwarn;
					for (int w : config.backupWarnTimes) {
						if (w != 0 && w + i == config.backupInterval) {
							
						} else {warn = false;}
					}

					if (warn) {
						// Perform warning
						if (config.backupEnabled) {
							plugin.getServer().broadcastMessage(Generic.parseColor(configmsg.messageBackupWarning));
							log.info(String.format("[%s] %s", plugin.getDescription().getName(), configmsg.messageBackupWarning));
						}
					}
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					log.info("Could not sleep!");
				}
			}
				if (config.backupEnabled||command) {performBackup();}
		}
		
		if (config.varDebug) {log.info("[AutoSaveWorld] Graceful quit of AutoBackupThread");}
	}
	
	
	private int numberofbackupsext = 0;
	private List<Long> backupnamesext;
	private int numberofbackupspl = 0;
	private List<Long> backupnamespl;
	public void loadConfigBackupExt(String extpath){
		configbackup = YamlConfiguration.loadConfiguration(new File(extpath+File.separator+"backups"+File.separator+"backups.yml"));
		numberofbackupsext = configbackup.getInt("worlds.numberofbackups", 0);
		backupnamesext = configbackup.getLongList("worlds.listnames");
		numberofbackupspl = configbackup.getInt("plugins.numberofbackups", 0);
		backupnamespl = configbackup.getLongList("plugins.listnames");
		
	}
	
	
	public void saveConfigBackupExt(String extpath){
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
	
    
	
	//all==true- * in worlds list;
	public void backupWorlds(List<String> worldNames, final boolean zip, final String extpath)
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
		if (config.backupWorlds.contains("*")) {all = true;}
		List<World> worlds = plugin.getServer().getWorlds();
		for (final World world : worlds) {
		if (worldNames.contains(world.getWorldFolder().getName())||all) {
				//create runnable for ThreadPoolExecutor
				Runnable worldb;

					worldb = new Runnable()
					{
						World worldt = world;
						String datebackup = new java.text.SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(datesec);

						boolean zipf = zip;
						public void run()
						{
							plugin.debug("Backuping world "+worldt.getWorldFolder().getName());

							worldt.setAutoSave(false);
							try {
								File worldfolder = worldt.getWorldFolder().getCanonicalFile();
								String pathtoworldsb = extpath+File.separator+"backups"+File.separator+"worlds"+File.separator+worldt.getWorldFolder().getName()+File.separator+datebackup;
								if (!zipf) {
									copyDirectory((worldfolder), new File(pathtoworldsb));
								} else { 
									Zip zipfld = new Zip(config);
									zipfld.ZipFolder((worldfolder), new File(pathtoworldsb+".zip"));
								}
							} catch(Exception e) {worldt.setAutoSave(true); plugin.debug("Failed to backup world "+worldt.getWorldFolder().getName()); e.printStackTrace();}
							worldt.setAutoSave(true);
							plugin.debug("Backuped world "+worldt.getWorldFolder().getName());
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

	
	
	public void backupPlugins(boolean zip, String extpath) {
		try {
			String datestring = new java.text.SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(datesec);
			String foldercopyto = extpath+File.separator+"backups"+File.separator+"plugins"+File.separator+datestring;
			if (!zip) {
				copyDirectory(new File((new File(".").getCanonicalPath())+File.separator+"plugins"),new File(foldercopyto));
			} else 
			{
				Zip zipfld = new Zip(config);
				zipfld.ZipFolder(new File((new File(".").getCanonicalPath())+File.separator+"plugins"),new File(foldercopyto+".zip"));
			}
		} catch (IOException e) {e.printStackTrace();}
	}


	
	public void performBackup() {
		if (plugin.backupInProgress) {
			plugin.warn("Multiple concurrent backups attempted! Backup interval is likely too short!");
			return;
		} else if (plugin.purgeInProgress) {
			plugin.warn("AutoPurge is in progress. Backup cancelled.");
			return;
		} else if (plugin.saveInProgress) {
			plugin.warn("AutoSave is in progress. Backup cancelled.");	
			return;
		} else {
			
		// Lock
		plugin.saveInProgress = true;
		plugin.backupInProgress = true;
		if (config.backupBroadcast){plugin.broadcast(configmsg.messageBroadcastBackupPre);}
		
		boolean zip = config.backupzip;
	    List<String> backupfoldersdest = new ArrayList<String>();
		datesec = System.currentTimeMillis();

		//adding internal folder to list of folders to which we should backup everything 
		if (!(config.donotbackuptointfld && config.backuptoextfolders))  {
			try {
				backupfoldersdest.add(new File(".").getCanonicalPath());
			} catch (IOException e) {e.printStackTrace();}
		}
		//adding external folders to list of folders to which we should backup everything 
		if (config.backuptoextfolders) {backupfoldersdest.addAll(config.extfolders);}
		
		//backup time	
		for (String extpath : backupfoldersdest) {
			//load info about backups stored in file backups.yml
			loadConfigBackupExt(extpath);
			//start worlds backup

			//delete oldest backup if needed
			if (!(config.MaxNumberOfWorldsBackups == 0) && (numberofbackupsext >= config.MaxNumberOfWorldsBackups)) {
				plugin.debug("Deleting oldest worlds backup");
				String datebackup = new java.text.SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(backupnamesext.get(0));
				String pathtoworldsfld = extpath+File.separator+"backups"+File.separator+"worlds";
				//delete worlds oldest backup
				for (String deldir : new File(pathtoworldsfld).list())
				{
					String dirtodelete = pathtoworldsfld+File.separator+deldir+File.separator+datebackup;
					deleteDirectory(new File(dirtodelete)); 
					deleteDirectory(new File(dirtodelete+".zip"));
				}
				backupnamesext.remove(0);
				numberofbackupsext--;}
			//do backup
			plugin.debug("Backuping Worlds");
			backupWorlds(config.backupWorlds, zip, extpath);
			plugin.debug("Backuped Worlds");
			backupnamesext.add(datesec);
			numberofbackupsext++;

			//now do plugins backup
			if (config.backuppluginsfolder) {
				//remove oldest backups
				if (!(config.MaxNumberOfPluginsBackups == 0) && (numberofbackupspl >= config.MaxNumberOfPluginsBackups)) {
					plugin.debug("Deleting oldest plugins backup");
					String datebackup = new java.text.SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(backupnamespl.get(0));
					String fldtodel = extpath+File.separator+"backups"+File.separator+"plugins"+File.separator+datebackup;
					//delete plugins oldest backup
						if (new File(fldtodel).exists())  {//exists if zip is false 
							deleteDirectory(new File(fldtodel)); }
						else {//not exists if zip is true
							deleteDirectory(new File(fldtodel+".zip"));}
					backupnamespl.remove(0);
					numberofbackupspl--;}	
				
				//do backup
				plugin.debug("Backuping plugins");
				backupPlugins(zip,extpath);
				plugin.debug("Backuped plugins");
				backupnamespl.add(datesec);
				numberofbackupspl++;
			}
			
			//save info about backups
			saveConfigBackupExt(extpath);
			
		}
		
		command = false;
		plugin.debug("Full backup time: "+(System.currentTimeMillis()-datesec)+" milliseconds");
		if (config.backupBroadcast){plugin.broadcast(configmsg.messageBroadcastBackupPost);}
		plugin.LastBackup =new java.text.SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(java.util.Calendar.getInstance ().getTime());
		// Release
		plugin.saveInProgress = false;
		plugin.backupInProgress = false;
		}
	}


	

	
	public void copyDirectory(File sourceLocation , File targetLocation)
			throws IOException {
			    if (sourceLocation.isDirectory()) {
			        if (!targetLocation.exists()) {
			            targetLocation.mkdirs();
			        }
			        
			        String[] children = sourceLocation.list();
			        for (int i=0; i<children.length; i++) {
			        	boolean copy = true;
			        	for (String efname : config.excludefolders)
			        	{
			        	if ((new File(sourceLocation, children[i]).getAbsoluteFile()).equals(new File(efname).getAbsoluteFile())) {copy = false; break;}
			        	}
			        	if (copy) {
			            copyDirectory(new File(sourceLocation, children[i]),
			                    new File(targetLocation, children[i])); 
			        	}
			        }
			    } else {
			        
			        try {
			    	InputStream in = new FileInputStream(sourceLocation);
			        OutputStream out = new FileOutputStream(targetLocation);

			        // Copy the bits from instream to outstream
			        byte[] buf = new byte[4096];
			        int len;
			        while ((len = in.read(buf)) > 0) {
			            out.write(buf, 0, len);
			           
			        }
			        in.close();
			        out.close();

			        } catch (IOException e) {plugin.debug("Failed to backup file "+sourceLocation); e.printStackTrace();}
			    }
			    }
			
	public void deleteDirectory(File file)
	  {
	    if(!file.exists())
	      return;
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


