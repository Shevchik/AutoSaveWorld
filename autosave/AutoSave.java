/**
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 3
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
*
*/

package autosave;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;


public class AutoSave extends JavaPlugin  {
private static final Logger log = Logger.getLogger("Minecraft");

public AutoSaveThread saveThread = null;
public AutoBackupThread6 backupThread6 = null;
public AutoPurgeThread purgeThread = null;
public SelfRestartThread selfrestartThread = null;
public CrashRestartThread crashrestartThread = null;
public JVMshutdownhook JVMsh = null;
private AutoSaveConfigMSG configmsg;
private AutoSaveConfig config;
private ASWEventListener eh;
protected int numPlayers = 0;
protected boolean saveInProgress = false;
protected boolean backupInProgress = false;
protected boolean purgeInProgress = false;
protected String LastSave = "No save was since the server start";
protected String LastBackup = "No backup was since the server start";
@Override
public void onDisable() {
if (crashrestartThread.restart)
{log.info("[AutoSaveWorld]Crash occured, restarting server.");}
// Perform a Save NOW!
saveThread.command=true;
saveThread.performSave();
//Stop threads
debug("Stopping Threads");
stopThread(ThreadType.SAVE);
stopThread(ThreadType.BACKUP6);
stopThread(ThreadType.PURGE);
if (!selfrestartThread.restart)
{stopThread(ThreadType.SELFRESTART);
log.info("[AutoSaveWorld] Graceful quit of selfrestart thread");}
if (crashrestartThread.restart)
{restart();}
else
{stopThread(ThreadType.CRASHRESTART);
JVMsh = null;
}

log.info(String.format("[%s] Version %s is disabled",getDescription().getName(),getDescription().getVersion()));
}

@Override
public void onEnable() {
// Load Configuration
File crfile = new File("plugins/ASWrestartmarker");
if (crfile.exists())
{
log.info("[AutoSaveWorld] Crash marker found, waiting for old server to finish");
while (true)
{
FileConfiguration restartfile = YamlConfiguration.loadConfiguration(crfile);
if (restartfile.getInt("restart",0)== 0) 
	{
	crfile.delete();
	log.info("[AutoSaveWorld] Old server finished, continuing start");
	break;
	} else
	{try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}}
}
}
config = new AutoSaveConfig(getConfig());
configmsg = new AutoSaveConfigMSG(config);
config.load();
configmsg.loadmsg();
config.loadbackupextfolderconfig();
eh = new ASWEventListener(this, config, configmsg);
//register events and commands
getCommand("autosaveworld").setExecutor(eh);
getCommand("autosave").setExecutor(eh);
getCommand("autobackup").setExecutor(eh);
getCommand("autopurge").setExecutor(eh);
getServer().getPluginManager().registerEvents(eh, this);
// Start AutoSave Thread
startThread(ThreadType.SAVE);
//Start AutoBackupThread
startThread(ThreadType.BACKUP6);
//Start AutoPurgeThread
startThread(ThreadType.PURGE);
//Start SelfRestarThread
startThread(ThreadType.SELFRESTART);
//Start CrashRestartThread
startThread(ThreadType.CRASHRESTART);
//Create JVMsh
JVMsh = new JVMshutdownhook();
// Notify on logger load
log.info(String.format("[%s] Version %s is enabled: %s", getDescription().getName(), getDescription().getVersion(), config.varUuid.toString()));
}


public void restart()
{
try {
	File restartscript = new File(config.crashrestartscriptpath);
	FileConfiguration restartfile = YamlConfiguration.loadConfiguration(new File("plugins/ASWrestartmarker"));
	restartfile.set("restart", 1);
	restartfile.save(new File("plugins/ASWrestartmarker"));
	Runtime.getRuntime().addShutdownHook(JVMsh);
	if (restartscript.exists()) {
	String OS = System.getProperty("os.name").toLowerCase();
	if (OS.contains("win")) {
		Runtime.getRuntime().exec("cmd /c start " + restartscript.getPath());
	} else {
		Runtime.getRuntime().exec(restartscript.getPath());
	}
	} else {
	System.out.println("[AutoSaveWorld] Startup script not found. CrashRestart failed");	
	}
} catch (Exception e)
{System.out.println("[AutoSaveWorld] CrashRestart failed");
e.printStackTrace();}
}


protected boolean startThread(ThreadType type) {
switch (type) {
case SAVE:
if (saveThread == null || !saveThread.isAlive()) {
saveThread = new AutoSaveThread(this, config, configmsg);
saveThread.start();
}
return true;
case BACKUP6:
if (backupThread6 == null || !backupThread6.isAlive()) {
backupThread6 = new AutoBackupThread6(this, config, configmsg);
backupThread6.start();
}
return true;
case PURGE:
if (purgeThread == null || !purgeThread.isAlive()) {
purgeThread = new AutoPurgeThread(this, config, configmsg);
purgeThread.start();
}
return true;
case SELFRESTART:
if (selfrestartThread == null || !selfrestartThread.isAlive()) {
selfrestartThread = new SelfRestartThread(this);
selfrestartThread.start();
}
return true;
case CRASHRESTART:
if (crashrestartThread == null || !crashrestartThread.isAlive()) {
crashrestartThread = new CrashRestartThread(this, config);
crashrestartThread.start();
}
return true;
default:
return false;
}
}

protected boolean stopThread(ThreadType type) {
switch (type) {
case SAVE:
if (saveThread == null) {
return true;
} else {
saveThread.setRun(false);
try {
saveThread.join(1000);
saveThread = null;
return true;
} catch (InterruptedException e) {
warn("Could not stop AutoSaveThread", e);
return false;
}
}
case BACKUP6:
if (backupThread6 == null) {
return true;
} else {
backupThread6.setRun(false);
try {
backupThread6.join(1000);
backupThread6 = null;
return true;
} catch (InterruptedException e) {
warn("Could not stop AutoBackupThread", e);
return false;
}
}
case PURGE:
if (purgeThread == null) {
return true;
} else {
purgeThread.setRun(false);
try {
purgeThread.join(1000);
purgeThread = null;
return true;
} catch (InterruptedException e) {
warn("Could not stop AutoPurgeThread", e);
return false;
}
}
case SELFRESTART:
if (selfrestartThread == null) {
return true;
} else {
selfrestartThread.stopthread();
try {
selfrestartThread.join(1000);
selfrestartThread = null;
return true;
} catch (InterruptedException e) {
warn("Could not stop SelfRestartThread", e);
return false;
}
}
case CRASHRESTART:
if (crashrestartThread == null) {
return true;
} else {
crashrestartThread.stopthread();
try {
crashrestartThread.join(1000);
crashrestartThread = null;
return true;
} catch (InterruptedException e) {
warn("Could not stop SelfRestartThread", e);
return false;
}
}
default:
return false;
}
}


public void sendMessage(CommandSender sender, String message) {
if (!message.equals("")) {
sender.sendMessage(Generic.parseColor(message));
}
}

public void broadcasta(String message) {
if (!message.equals("") && (config.varBroadcast)) {
getServer().broadcastMessage(Generic.parseColor(message));
log.info(String.format("[%s] %s", getDescription().getName(), Generic.stripColor(message)));
}

}
public void broadcastb(String message) {
if (!message.equals("") && (config.backupBroadcast)) {
getServer().broadcastMessage(Generic.parseColor(message));
log.info(String.format("[%s] %s", getDescription().getName(), Generic.stripColor(message)));
}

}
public void broadcastc(String message) {
if (!message.equals("") && (config.purgeBroadcast)) {
getServer().broadcastMessage(Generic.parseColor(message));
log.info(String.format("[%s] %s", getDescription().getName(), Generic.stripColor(message)));
}

}

public void debug(String message) {
if (config.varDebug) {
log.info(String.format("[%s] %s", getDescription().getName(), Generic.stripColor(message)));
}
}

public void warn(String message) {
log.warning(String.format("[%s] %s", getDescription().getName(), Generic.stripColor(message)));
}

public void warn(String message, Exception e) {
log.log(Level.WARNING, String.format("[%s] %s", getDescription().getName(), Generic.stripColor(message)), e);
}

}
