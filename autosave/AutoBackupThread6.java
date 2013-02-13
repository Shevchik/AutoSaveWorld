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
//This code is not supported, because java6 is outdated, but i will still keep this class in case somebody can't update to java7.
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.World;

public class AutoBackupThread6 extends Thread {

	// Constructor to define number of seconds to sleep
	AutoBackupThread6(AutoSave plugin, AutoSaveConfig config, AutoSaveConfigMSG configmsg) {
		this.plugin = plugin;
		this.config = config;
		this.configmsg = configmsg;
	}
	
	protected final Logger log = Logger.getLogger("Minecraft");
	private boolean run = true;
	private AutoSave plugin = null;
	private AutoSaveConfig config;
	private AutoSaveConfigMSG configmsg;
	private Zip zipfld = null;
    public long datesec;
    private List<Long> tempnames = new ArrayList<Long>();
    private int runnow;
    private boolean command = false;

    
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
		while (run) {
			// Prevent AutoBackup from never sleeping
			// If interval is 0, sleep for 5 seconds and skip saving
			if(config.backupInterval == 0) {
				try {
					Thread.sleep(5000);
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
    
	//all==true- * in worlds list; ext==true - copy external folders
	private int backupWorlds(List<String> worldNames,boolean ext, boolean zip) {
		// Save our worlds...
		boolean all= false;
		if (config.varWorlds.contains("*")) {all = true;}
			int i = 0;
			List<World> worlds = plugin.getServer().getWorlds();
			for (World world : worlds) {
			if (worldNames.contains(world.getName())||all) {
			plugin.debug(String.format("Backuping world: %s", world.getName()));
			if (ext) {
			try {
				copyDirectory(new File(new File(".").getCanonicalPath()+File.separator+world.getName()), new File(config.extpath+File.separator+"backups"+File.separator+datesec+File.separator+world.getName()));
				if (zip) zipfld.ZipFolder(new File(config.extpath+File.separator+"backups"+File.separator+datesec+File.separator+world.getName()));
				if (zip) deleteDirectory(new File(config.extpath+File.separator+"backups"+File.separator+datesec+File.separator+world.getName()));
			
			} catch (IOException e) {e.printStackTrace();} 
			
			} else {
				
				try {
					copyDirectory(new File(new File(".").getCanonicalPath()+File.separator+world.getName()), new File(new File(".").getCanonicalPath()+File.separator+"backups"+File.separator+datesec+File.separator+world.getName()));
					if (zip) zipfld.ZipFolder(new File(new File(".").getCanonicalPath()+File.separator+"backups"+File.separator+datesec+File.separator+world.getName()));
					if (zip) deleteDirectory(new File(new File(".").getCanonicalPath()+File.separator+"backups"+File.separator+datesec+File.separator+world.getName()));
				} catch (IOException e) {e.printStackTrace();}
			} 
			i++;
			}
			}
			return i;	
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
			        byte[] buf = new byte[5120];
			        int len;
			        while ((len = in.read(buf)) > 0) {
			            out.write(buf, 0, len);
			           
			        }
			        in.close();
			        out.close();
				    if (config.slowbackup) {
					    try {
							sleep(0);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}}
			        } catch (IOException e) {System.out.println("Failed to backup file "+sourceLocation);}

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
		plugin.broadcastb(configmsg.messageBroadcastBackupPre);
		datesec = System.currentTimeMillis();
		int saved = 0;
		//config
		if (config.donotbackuptointfld && config.backuptoextfolders) {} else {
		config.loadConfigBackup();
		//check if the backups are still here
		tempnames.clear();
		for (long name : config.backupnames) {
		if (new File("backups"+File.separator+name).exists()) {tempnames.add(name);};
		}
		config.backupnames.clear();
		for (long name : tempnames) {config.backupnames.add(name);}
		config.numberofbackups = config.backupnames.size();
		//delete oldest backup if needed
		if (!(config.MaxNumberOfBackups == 0) && (config.numberofbackups >= config.MaxNumberOfBackups))
		{try {
			plugin.debug("Deleting oldest backup");
			deleteDirectory(new File(new File(".").getCanonicalPath()+File.separator+"backups"+File.separator+config.backupnames.get(0).toString()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		};
		config.backupnames.remove(0);
		config.numberofbackups--;}
		
		//do backup

		saved += backupWorlds(config.varWorlds, false, zip);
		plugin.debug(String.format("Backup %d Worlds", saved));
		config.backupnames.add(datesec);
		config.numberofbackups++;
		//black magic...
		config.saveConfigBackup();
		config.datesec = datesec;
		config.getbackupdate();
		if (config.backuppluginsfolder) {
			try {
			plugin.debug("Backuping plugins");
			copyDirectory(new File((new File(".").getCanonicalPath())+File.separator+"plugins"),new File("backups"+File.separator+datesec+File.separator+"plugins"));
			if (zip) zipfld.ZipFolder(new File("backups"+File.separator+datesec+File.separator+"plugins"));
			if (zip) deleteDirectory(new File("backups"+File.separator+datesec+File.separator+"plugins"));
			} catch (IOException e) {
			e.printStackTrace();
			}
		}
		}
		//now backup to ext folders
		//config
		if (config.backuptoextfolders) {
		if (config.varDebug) {plugin.debug("start extbackup");};
		config.loadbackupextfolderconfig();
		for (String extpath : config.extfolders) {
		config.extpath = extpath;
		if (config.varDebug){plugin.debug("Path is:"+config.extpath);};
		config.loadConfigBackupExt();
		//check if the backups are still here
		tempnames.clear();
		for (long name : config.backupnamesext) {
		if ((new File(config.extpath+File.separator+"backups"+File.separator+name).exists())) {tempnames.add(name);};}
		config.backupnamesext.clear();
		for (long name : tempnames) {config.backupnamesext.add(name);}
		config.numberofbackupsext = config.backupnamesext.size();
		if(config.varDebug){plugin.debug("configuring done");};
		//delete oldest backup if needed
		if (!(config.MaxNumberOfBackups == 0) && (config.numberofbackupsext >= config.MaxNumberOfBackups)) {
			plugin.debug("Deleting oldest backup");
			deleteDirectory(new File(config.extpath+File.separator+"backups"+File.separator+config.backupnamesext.get(0).toString())); 		
			config.backupnamesext.remove(0);
			config.numberofbackupsext--;}
		//do backup
		saved = 0;
		saved += backupWorlds(config.varWorlds, true, zip);
		plugin.debug(String.format("Backup %d Worlds", saved));
		config.backupnamesext.add(datesec);
		config.numberofbackupsext++;
		config.saveConfigBackupExt(); 
		config.datesec = datesec;
		config.getbackupdateext(); 
			if (config.backuppluginsfolder) {

				try {
					plugin.debug("Backuping plugins");
					copyDirectory(new File((new File(".").getCanonicalPath())+File.separator+"plugins"),new File(config.extpath+File.separator+"backups"+File.separator+datesec+File.separator+"plugins"));
					if (zip) zipfld.ZipFolder(new File(config.extpath+File.separator+"backups"+File.separator+datesec+File.separator+"plugins"));
					if (zip) deleteDirectory(new File(config.extpath+File.separator+"backups"+File.separator+datesec+File.separator+"plugins"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			
			}
		}
		}
		command = false;
		plugin.broadcastb(configmsg.messageBroadcastBackupPost);
		// Release
		plugin.saveInProgress = false;
		plugin.backupInProgress = false;
		if (config.varDebug) {plugin.debug("Full backup time: "+(System.currentTimeMillis()-datesec)+" milliseconds");}
		if (config.slowbackup) {setPriority(Thread.NORM_PRIORITY);}
		}
		}


	

	}


