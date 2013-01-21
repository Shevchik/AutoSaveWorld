/**
*
* Copyright 2011 MilkBowl (https://github.com/MilkBowl)
* Copyright 2012 Shevchik
* 
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


//Java6 is no longer supported, backup with java6 won't have all features that backup with7 will have
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;


public class AutoSave extends JavaPlugin  {
private static final Logger log = Logger.getLogger("Minecraft");

public AutoSaveThread saveThread = null;
public AutoBackupThread6 backupThread6 = null;
public AutoBackupThread7 backupThread7 = null;
private AutoSaveConfigMSG configmsg;
private AutoSaveConfig config;
private ASWEventListener eh;
protected int numPlayers = 0;
protected boolean saveInProgress = false;
protected boolean backupInProgress = false;


@Override
public void onDisable() {
// Perform a Save NOW!
saveThread.startsave();

try {
for (World name : getServer().getWorlds()) {
	name.setAutoSave(true);
}
} catch (Exception e) {}
//Stop threads
debug(String.format("[%s] Stopping Threads",
getDescription().getName()));

stopThread(ThreadType.SAVE);
if (config.javanio)
{stopThread(ThreadType.BACKUP7);}
else {stopThread(ThreadType.BACKUP6);}

log.info(String.format("[%s] Version %s is disabled",getDescription().getName(),getDescription().getVersion()));

}

@Override
public void onEnable() {
// Load Configuration
config = new AutoSaveConfig(getConfig());
configmsg = new AutoSaveConfigMSG(getConfig());
config.load();
configmsg.loadmsg();
config.loadbackupextfolderconfig();
eh = new ASWEventListener(this, config, configmsg);
//register events and commands
getCommand("autosaveworld").setExecutor(eh);
getServer().getPluginManager().registerEvents(eh, this);
//Check if we have java7.
try{java.nio.file.Files.class.getMethods();
config.javanio = true;
debug("java7");
	} catch (NoClassDefFoundError e) {
config.javanio = false;
debug("java6");
	} catch (SecurityException e) {
		// TODO Auto-generated catch block
		config.javanio = false;
	}
//Disable internal autosave
try {
for (World name : getServer().getWorlds()) {
	name.setAutoSave(false);
}
} catch (Exception e) {}
// Start AutoSave Thread
startThread(ThreadType.SAVE);
//Start AutoBackupThread
if (config.javanio) {
startThread(ThreadType.BACKUP7); }
else {
startThread(ThreadType.BACKUP6);}
// Notify on logger load
log.info(String.format("[%s] Version %s is enabled: %s", getDescription().getName(), getDescription().getVersion(), config.varUuid.toString()));
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
case BACKUP7:
if (backupThread7 == null || !backupThread7.isAlive()) {
backupThread7 = new AutoBackupThread7(this, config, configmsg);
backupThread7.start();
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
saveThread.join(5000);
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
backupThread6.join(5000);
backupThread6 = null;
return true;
} catch (InterruptedException e) {
warn("Could not stop AutoBackupThread", e);
return false;
}
}
case BACKUP7:
if (backupThread7 == null) {
return true;
} else {
backupThread7.setRun(false);
try {
backupThread7.join(5000);
backupThread7 = null;
return true;
} catch (InterruptedException e) {
warn("Could not stop AutoBackupThread", e);
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












/*private void savePlayers() {
	// Save the players
	debug("Saving players");
	this.getServer().savePlayers();
	}

	private int saveWorlds() {
	// Save our worlds
	int i = 0;
	List<World> worlds = this.getServer().getWorlds();
	for (World world : worlds) {
	debug(String.format("Saving world: %s", world.getName()));
	world.save();
	i++;
	}
	return i;
	}


public void performSave() {
	if (saveInProgress) {
	warn("Multiple concurrent saves attempted! Save interval is likely too short!");
	return;
	}
	try {
	if (getServer().getOnlinePlayers().length == 0) {
	// No players online, don't bother saving.
	debug("Skipping save, no players online.");
	return;

	}

	// Lock
	saveInProgress = true;

	broadcasta(configmsg.messageBroadcastPre);

	// Save the players
	savePlayers();
	debug("Saved Players");

	// Save the worlds
	int saved = 0;
	saved += saveWorlds();

	debug(String.format("Saved %d Worlds", saved));

	broadcasta(configmsg.messageBroadcastPost);
	} catch (Exception e) 
	{broadcasta("&4Save Failed");}
	// Release
	saveInProgress = false;
	}
*/