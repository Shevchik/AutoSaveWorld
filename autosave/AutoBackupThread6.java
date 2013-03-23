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

package autosave;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class AutoBackupThread6 extends Thread {

	// Constructor to define number of seconds to sleep
	AutoBackupThread6(AutoSave plugin, AutoSaveConfig config, AutoSaveConfigMSG configmsg) {
		this.plugin = plugin;
		this.config = config;
		this.configmsg = configmsg;
	}
	
	protected final Logger log = Bukkit.getLogger();
	private boolean run = true;
	private AutoSave plugin = null;
	private AutoSaveConfig config;
	private AutoSaveConfigMSG configmsg;
	private Zip zipfld = null;
    public long datesec;
    private int runnow;
    private boolean command = false;
    private List<String> backupfoldersdest = new ArrayList<String>();
    private FileConfiguration configbackup;
    
	// Allows for the thread to naturally exit if value is false
	public void setRun(boolean run) {
		this.run = run;
	}
	
	public void startbackup()
	{
	command = true;
	runnow = config.backupInterval;
	}
    
	
	// The code to run...weee
	public void run() {
		if (config == null) {
			return;
		}

		log.info(String
				.format("[%s] AutoBackupThread Started: Interval is %d seconds, Warn Times are %s",
						plugin.getDescription().getName(), config.backupInterval,
						Generic.join(",", config.backupWarnTimes)));
		Thread.currentThread().setName("AutoSaveWorld_AutoBackupThread");
		while (run) {
			// Prevent AutoBackup from never sleeping
			// If interval is 0, sleep for 10 seconds and skip backup
			if(config.backupInterval == 0) {
				try {
					Thread.sleep(10000);
				} catch(InterruptedException e) {
					// care
				}
				continue;
			}
			
			// Do our Sleep stuff!
			for (runnow = 0; runnow < config.backupInterval; runnow++) {
				try {
					if (!run) {
						if (config.varDebug) {
							log.info(String.format("[%s] Graceful quit of AutoBackupThread", plugin.getDescription().getName()));
						}
						return;
					}
					boolean warn = config.backupwarn;
					for (int w : config.backupWarnTimes) {
						if (w != 0 && w + runnow == config.backupInterval) {
							
						} else {warn = false;}
					}

					if (warn) {
						// Perform warning
						if (config.varDebug) {
							log.info(String.format("[%s] Warning Time Reached: %d seconds to go.", plugin.getDescription().getName(), config.backupInterval - runnow));
						}
						if (config.backupEnabled) {plugin.getServer().broadcastMessage(Generic.parseColor(configmsg.messageBackupWarning));}
						log.info(String.format("[%s] %s", plugin.getDescription().getName(), configmsg.messageBackupWarning));
					}
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					log.info("Could not sleep!");
				}
			}
				if (config.backupEnabled||command) {performBackup();}
			}
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
	
    
	//all==true- * in worlds list; ext==true - copy external folders
	private int backupWorlds(List<String> worldNames, boolean zip, String extpath) {
		// Save our worlds...
		boolean all= false;
		if (config.backupWorlds.contains("*")) {all = true;}
			int i = 0;
			List<World> worlds = plugin.getServer().getWorlds();
			for (World world : worlds) {
			if (worldNames.contains(world.getWorldFolder().getName())||all) {
			String worldfoldername = world.getWorldFolder().getName();
			plugin.debug(worldfoldername);
			plugin.debug(String.format("Backuping world: %s", world.getName()));
			try {
				String datebackup = new java.text.SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(datesec);
				String pathtoworldsb = extpath+File.separator+"backups"+File.separator+"worlds"+File.separator+worldfoldername+File.separator+datebackup;
				if (!zip) {
				copyDirectory(new File(new File(".").getCanonicalPath()+File.separator+worldfoldername), new File(pathtoworldsb));
				} else 
				{ zipfld.ZipFolder(new File(new File(".").getCanonicalPath()+File.separator+worldfoldername), new File(pathtoworldsb+".zip"));}
			
			} catch (IOException e) {e.printStackTrace();} 
			} 
			i++;
			}
			return i;	
		}
	public void backupPlugins(boolean zip, String extpath) {
		try {
			plugin.debug("Backuping plugins");
			String datestring = new java.text.SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(datesec);
			String foldercopyto = extpath+File.separator+"backups"+File.separator+"plugins"+File.separator+datestring;
			if (!zip) {
			copyDirectory(new File((new File(".").getCanonicalPath())+File.separator+"plugins"),new File(foldercopyto));
			} else 
			{zipfld.ZipFolder(new File((new File(".").getCanonicalPath())+File.separator+"plugins"),new File(foldercopyto+".zip"));}
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		}	else {
		if (config.slowbackup) {setPriority(Thread.MIN_PRIORITY);}
		boolean zip = config.backupzip;
		if (zip) {
		if (zipfld == null) {zipfld = new Zip();}	
		}
		
		// Lock
		plugin.saveInProgress = true;
		plugin.backupInProgress = true;
		if (config.backupBroadcast){plugin.broadcast(configmsg.messageBroadcastBackupPre);}
		datesec = System.currentTimeMillis();
		int saved = 0;
		backupfoldersdest.clear();
		//adding internal folder to list of folders to which we should backup everything 
		if (config.donotbackuptointfld && config.backuptoextfolders) {} 
		else {
			try {backupfoldersdest.add(new File(".").getCanonicalPath());
			} catch (IOException e) {e.printStackTrace();}
		}
		//adding external folders to list of folders to which we should backup everything 
		if (config.backuptoextfolders) {
			config.loadbackupextfolderconfig();
			for (String extpath : config.extfolders) {
				backupfoldersdest.add(extpath);
			}	
		}
		
		//backup time	
		for (String extpath : backupfoldersdest) {
			//load info about backups stored in file backups.yml
			loadConfigBackupExt(extpath);
			//start worlds backup

			//delete oldest backup if needed
			if (!(config.MaxNumberOfWorldsBackups == 0) && (numberofbackupsext >= config.MaxNumberOfWorldsBackups)) {
				plugin.debug("Deleting oldest backup");
				String datebackup = new java.text.SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(backupnamesext.get(0));
				String pathtoworldsfld = extpath+File.separator+"backups"+File.separator+"worlds";
				//delete worlds oldest backup
				for (String deldir : new File(pathtoworldsfld).list())
				{
					File dirtodelete = new File(pathtoworldsfld+File.separator+deldir+File.separator+datebackup);
					if (dirtodelete.exists())  {//exists if zip is false 
						plugin.debug(dirtodelete.toString());
						deleteDirectory(dirtodelete); }
					else {//not exists if zip is true
						plugin.debug(new File(dirtodelete+".zip").toString());
						deleteDirectory(new File(dirtodelete+".zip"));}
				}
				backupnamesext.remove(0);
				numberofbackupsext--;}
			//do backup
			saved = 0;
			saved += backupWorlds(config.backupWorlds, zip, extpath);
			plugin.debug(String.format("Backuped %d Worlds", saved));
			backupnamesext.add(datesec);
			numberofbackupsext++;

			//now do plugins backup
			if (config.backuppluginsfolder) {
				//remove oldest backups
				if (!(config.MaxNumberOfPluginsBackups == 0) && (numberofbackupspl >= config.MaxNumberOfPluginsBackups)) {
					plugin.debug("Deleting oldest backup");
					String datebackup = new java.text.SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(backupnamespl.get(0));
					String fldtodel = extpath+File.separator+"backups"+File.separator+"plugins"+File.separator+datebackup;
					//delete worlds oldest backup
						if (new File(fldtodel).exists())  {//exists if zip is false 
							plugin.debug(fldtodel.toString());
							deleteDirectory(new File(fldtodel)); }
						else {//not exists if zip is true
							plugin.debug(new File(fldtodel+".zip").toString());
							deleteDirectory(new File(fldtodel+".zip"));}
					backupnamespl.remove(0);
					numberofbackupspl--;}	
				
				//do backups
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
		if (config.slowbackup) {setPriority(Thread.NORM_PRIORITY);}
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
			            copyDirectory(new File(sourceLocation, children[i]),
			                    new File(targetLocation, children[i])); 
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

			        } catch (IOException e) {plugin.debug("Failed to backup file "+sourceLocation);}
				    if (config.slowbackup) {
					    try {
							sleep(0);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}}

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


