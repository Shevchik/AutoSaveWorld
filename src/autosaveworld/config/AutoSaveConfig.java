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

package autosaveworld.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import autosaveworld.core.AutoSaveWorld;


public class AutoSaveConfig {

	private FileConfiguration config;
	private AutoSaveWorld plugin;
	public AutoSaveConfig(AutoSaveWorld plugin) {
		this.plugin = plugin;
	}



	// some global variables
	public boolean varDebug = false;
	public boolean commandonlyfromconsole = false;
	//save
	public int saveInterval = 900;
	public boolean saveBroadcast = true;
	public boolean saveEnabled = true;
	public boolean donotsavestructures = false;
	public boolean saveOnASWDisable = true;
	public boolean saveOnLastQuit = true;
	public boolean saveIgnoreIfNoPlayers = true;
	//backup
	public boolean backupEnabled = false;
	public int backupInterval =  60*60*6;
	public boolean backupBroadcast = true;
	public boolean backupsaveBefore = true;
	//localfs backup
	public boolean localfsbackupenabled = true;
	public List<String> lfsbackupWorldsList = null;
	public int lfsMaxNumberOfWorldsBackups = 15;
	public boolean lfsbackuppluginsfolder = false;
	public int lfsMaxNumberOfPluginsBackups = 15;
	public List<String> lfsextfolders;
	public List<String> lfsbackupexcludefolders;
	public boolean lfsbackupzip = false;
	//ftp backup
	public boolean ftpbackupenabled = false;
	public String ftphostname = "127.0.0.1";
	public int ftpport = 21;
	public String ftpusername = "user";
	public String ftppassworld = "password";
	public String ftppath = "/asw/";
	public List<String> ftpbackupWorlds = null;
	public boolean ftpbackuppluginsfolder = false;
	public List<String> ftpbackupexcludefolders;
	public int ftpbackupmaxnumberofbackups = 4;
	public boolean ftpbackupzip = false;
	//script
	public boolean scriptbackupenabled = false;
	public List<String> scriptbackupscriptpaths = new ArrayList<String>();
	//purge
	public int purgeInterval = 60*60*24;
	public long purgeAwayTime = 60*60*24*30;
	public boolean purgeEnabled = false;
	public boolean purgeBroadcast = true;
	public boolean purgewg = true;
	public boolean purgewgregenrg = false;
	public boolean purgewgnoregenoverlap = true;
	public boolean purgelwc = true;
	public boolean purgelwcdelprotectedblocks = false;
	public boolean purgemvinv = true;
	public boolean purgepm = true;
	public boolean purgepmregen = false;
	public boolean purgeresidence = true;
	public boolean purgeresregenarena = false;
	public boolean purgeeconomy = true;
	public boolean purgeperms = true;
	public boolean purgedat = true;
	//crashrestart
	public boolean crashrestartenabled = false;
	public String crashrestartscriptpath="";
	public boolean crstop = false;
	public long crtimeout = 60;
	public int crdelay = 20;
	//autorestart
	public boolean autorestart = false;
	public boolean autorestartBroadcast = true;
	public String autorestartscriptpath = "";
	public List<String> autorestarttime = new ArrayList<String>();
	public boolean autorestartcountdown = true;
	public List<Integer> autorestartbroadcastonseconds = new ArrayList<Integer>();
	public List<String> autorestartcommmands = new ArrayList<String>();
	public boolean astop = false;
	//consolecmmand
	public boolean cctimeenabled = false;
	public HashMap<String, List<String>> cctimescommands = new HashMap<String, List<String>>();
	public boolean ccintervalenabled = false;
	public HashMap<Integer, List<String>> ccintervalscommands = new HashMap<Integer, List<String>>();
	//worldregen
	public boolean worldregensavewg = true;
	public boolean worldregensavefactions = true;
	public boolean worldregensavegp = true;
	public boolean worldregensavetowny = true;


	//config load/save functions
	public void load() {

		config = YamlConfiguration.loadConfiguration(new File(plugin.constants.getConfigPath()));

		// Variables
		varDebug = config.getBoolean("var.debug", varDebug);
		commandonlyfromconsole = config.getBoolean("var.commandsonlyfromconsole",commandonlyfromconsole);

		//save variables
		saveEnabled = config.getBoolean("save.enabled",saveEnabled);
		donotsavestructures = config.getBoolean("save.disablestructuresaving", donotsavestructures);
		saveBroadcast = config.getBoolean("save.broadcast", saveBroadcast);
		saveInterval = config.getInt("save.interval", saveInterval);
		saveOnASWDisable = config.getBoolean("save.onplugindisable", saveOnASWDisable);
		saveIgnoreIfNoPlayers = config.getBoolean("save.donotsaveifempty", saveIgnoreIfNoPlayers);
		saveOnLastQuit = config.getBoolean("save.onlastplayerquit", saveOnLastQuit);


		//backup variables
		backupEnabled = config.getBoolean("backup.enabled", backupEnabled);
		backupInterval = config.getInt("backup.interval", backupInterval);
		backupBroadcast = config.getBoolean("backup.broadcast", backupBroadcast);
		backupsaveBefore = config.getBoolean("backup.savebefore",backupsaveBefore);
		//localfs
		localfsbackupenabled = config.getBoolean("backup.localfs.enabled",localfsbackupenabled);
		lfsMaxNumberOfWorldsBackups = config.getInt("backup.localfs.MaxNumberOfWorldsBackups", lfsMaxNumberOfWorldsBackups);
		lfsMaxNumberOfPluginsBackups = config.getInt("backup.localfs.MaxNumberOfPluginsBackups", lfsMaxNumberOfPluginsBackups);
		lfsextfolders = config.getStringList("backup.localfs.destinationfolders");
		if (lfsextfolders.isEmpty())
		{
			lfsextfolders.add(new File(".").getAbsolutePath());
		}
		lfsbackuppluginsfolder = config.getBoolean("backup.localfs.pluginsfolder", lfsbackuppluginsfolder);
		lfsbackupexcludefolders = config.getStringList("backup.localfs.excludefolders");
		lfsbackupzip = config.getBoolean("backup.localfs.zip", lfsbackupzip);
		lfsbackupWorldsList = config.getStringList("backup.localfs.worlds");
		if (lfsbackupWorldsList.size() == 0) {
			lfsbackupWorldsList.add("*");
		}
		//ftp
		ftpbackupenabled = config.getBoolean("backup.ftp.enabled",ftpbackupenabled);
		ftphostname = config.getString("backup.ftp.hostname",ftphostname);
		ftpport = config.getInt("backup.ftp.port",ftpport);
		ftpusername = config.getString("backup.ftp.login",ftpusername);
		ftppassworld = config.getString("backup.ftp.password",ftppassworld);
		ftppath = config.getString("backup.ftp.path",ftppath);
		ftpbackupWorlds = config.getStringList("backup.ftp.worlds");
		ftpbackuppluginsfolder = config.getBoolean("backup.ftp.pluginsfolder",ftpbackuppluginsfolder);
		ftpbackupexcludefolders = config.getStringList("backup.ftp.excludefolders");
		ftpbackupmaxnumberofbackups = config.getInt("backup.ftp.maxNumberOfBackups",ftpbackupmaxnumberofbackups);
		ftpbackupzip = config.getBoolean("backup.ftp.zip",ftpbackupzip);
		if (ftpbackupWorlds.size() == 0) {
			ftpbackupWorlds.add("*");
		}
		//script
		scriptbackupenabled = config.getBoolean("backup.script.enabled",scriptbackupenabled);
		scriptbackupscriptpaths = config.getStringList("backup.script.scriptpaths");



		//purge variables
		purgeInterval = config.getInt("purge.interval", purgeInterval);
		purgeAwayTime = config.getLong("purge.awaytime", purgeAwayTime);
		purgeEnabled = config.getBoolean("purge.enabled", purgeEnabled);
		purgeBroadcast = config.getBoolean("purge.broadcast", purgeBroadcast);
		purgewg = config.getBoolean("purge.wg.enabled", purgewg);
		purgewgregenrg = config.getBoolean("purge.wg.regenpurgedregion", purgewgregenrg);
		purgewgnoregenoverlap = config.getBoolean("purge.wg.noregenoverlapregion",purgewgnoregenoverlap);
		purgelwc = config.getBoolean("purge.lwc.enabled", purgelwc);
		purgelwcdelprotectedblocks = config.getBoolean("purge.lwc.deletepurgedblocks",purgelwcdelprotectedblocks);
		purgemvinv = config.getBoolean("purge.mvinv.enabled",purgemvinv);
		purgepm = config.getBoolean("purge.pm.enabled", purgepm);
		purgepmregen = config.getBoolean("purge.pm.regenpurgedplot",purgepmregen);
		purgeresidence = config.getBoolean("purge.residence.enabled", purgeresidence);
		purgeresregenarena = config.getBoolean("purge.residence.regenpurgedresidence",purgeresregenarena);
		purgeeconomy = config.getBoolean("purge.economy.enabled", purgeeconomy);
		purgeperms = config.getBoolean("purge.permissions.enabled", purgeperms);
		purgedat = config.getBoolean("purge.dat.enabled", purgedat);

		//crashrestart variables
		crashrestartenabled = config.getBoolean("crashrestart.enabled",crashrestartenabled);
		crdelay = config.getInt("crashrestart.startdelay",crdelay);
		crashrestartscriptpath = config.getString("crashrestart.scriptpath",crashrestartscriptpath);
		crtimeout = config.getLong("crashrestart.timeout",crtimeout);
		crstop = config.getBoolean("crashrestart.juststop", crstop);

		//autorestart variables
		autorestart = config.getBoolean("autorestart.enabled", autorestart);
		autorestartBroadcast = config.getBoolean("autorestart.broadcast", autorestartBroadcast);
		autorestarttime = config.getStringList("autorestart.time");
		autorestartcountdown = config.getBoolean("autorestart.countdown.enabled", autorestartcountdown);
		autorestartbroadcastonseconds = config.getIntegerList("autorestart.countdown.broadcastonsecond");
		autorestartcommmands = config.getStringList("autorestart.commands");
		autorestartscriptpath = config.getString("autorestart.scriptpath",autorestartscriptpath);
		astop = config.getBoolean("autorestart.juststop", astop);
		if (autorestartbroadcastonseconds.size() == 0)
		{
			autorestartbroadcastonseconds.add(60);
			autorestartbroadcastonseconds.add(30);
			for (int i = 1; i<=10; i++)
			{
				autorestartbroadcastonseconds.add(i);
			}
		}
		Collections.sort(autorestartbroadcastonseconds, Collections.reverseOrder());
		//autoconsolecommand variables
		cctimeenabled = config.getBoolean("consolecommand.timemode.enabled", cctimeenabled);
		cctimescommands.clear();
		ConfigurationSection cctimescs = config.getConfigurationSection("consolecommand.timemode.times");
		if (cctimescs != null)
		{
			for (String time : cctimescs.getKeys(false))
			{
				cctimescommands.put(time, cctimescs.getStringList(time));
			}
		}
		ccintervalenabled = config.getBoolean("consolecommand.intervalmode.enabled", ccintervalenabled);
		ccintervalscommands.clear();
		ConfigurationSection ccintervalscs = config.getConfigurationSection("consolecommand.intervalmode.intervals");
		if (ccintervalscs != null)
		{
			for (String interval : ccintervalscs.getKeys(false))
			{
				try {
					ccintervalscommands.put(Integer.valueOf(interval), ccintervalscs.getStringList(interval));
				} catch (Exception e) {}
			}
		}

		//worldregen variables
		worldregensavewg = config.getBoolean("worldregen.savewg",worldregensavewg);
		worldregensavefactions = config.getBoolean("worldregen.savefactions",worldregensavefactions);
		worldregensavegp = config.getBoolean("worldregen.savegp",worldregensavegp);
		worldregensavetowny = config.getBoolean("worldregen.savetowny",worldregensavetowny);

		save();
	}

	public void save() {
		config = new YamlConfiguration();


		// Variables
		config.set("var.debug", varDebug);
		config.set("var.commandsonlyfromconsole",commandonlyfromconsole);

		//save variables
		config.set("save.enabled",saveEnabled);
		config.set("save.disablestructuresaving", donotsavestructures);
		config.set("save.interval", saveInterval);
		config.set("save.broadcast", saveBroadcast);
		config.set("save.onplugindisable", saveOnASWDisable);
		config.set("save.onlastplayerquit", saveOnLastQuit);
		config.set("save.donotsaveifempty", saveIgnoreIfNoPlayers);

		//backup variables
		config.set("backup.enabled", backupEnabled);
		config.set("backup.interval", backupInterval);
		config.set("backup.savebefore",backupsaveBefore);
		config.set("backup.broadcast", backupBroadcast);
		//localfs
		config.set("backup.localfs.enabled",localfsbackupenabled);
		config.set("backup.localfs.worlds", lfsbackupWorldsList);
		config.set("backup.localfs.MaxNumberOfWorldsBackups", lfsMaxNumberOfWorldsBackups);
		config.set("backup.localfs.pluginsfolder", lfsbackuppluginsfolder);
		config.set("backup.localfs.MaxNumberOfPluginsBackups", lfsMaxNumberOfPluginsBackups);
		config.set("backup.localfs.excludefolders",lfsbackupexcludefolders);
		config.set("backup.localfs.destinationfolders",lfsextfolders);
		config.set("backup.localfs.zip",lfsbackupzip);
		//ftp
		config.set("backup.ftp.enabled",ftpbackupenabled);
		config.set("backup.ftp.hostname",ftphostname);
		config.set("backup.ftp.port",ftpport);
		config.set("backup.ftp.login",ftpusername);
		config.set("backup.ftp.password",ftppassworld);
		config.set("backup.ftp.path",ftppath);
		config.set("backup.ftp.worlds",ftpbackupWorlds);
		config.set("backup.ftp.pluginsfolder",ftpbackuppluginsfolder);
		config.set("backup.ftp.excludefolders",ftpbackupexcludefolders);
		config.set("backup.ftp.maxNumberOfBackups",ftpbackupmaxnumberofbackups);
		config.set("backup.ftp.zip",ftpbackupzip);
		//script
		config.set("backup.script.enabled",scriptbackupenabled);
		config.set("backup.script.scriptpaths",scriptbackupscriptpaths);


		//purge variables
		config.set("purge.enabled", purgeEnabled);
		config.set("purge.interval", purgeInterval);
		config.set("purge.awaytime", purgeAwayTime);
		config.set("purge.broadcast",purgeBroadcast);
		config.set("purge.wg.enabled", purgewg);
		config.set("purge.wg.regenpurgedregion", purgewgregenrg);
		config.set("purge.wg.noregenoverlapregion",purgewgnoregenoverlap);
		config.set("purge.lwc.enabled", purgelwc);
		config.set("purge.lwc.deletepurgedblocks", purgelwcdelprotectedblocks);
		config.set("purge.mvinv.enabled",purgemvinv);
		config.set("purge.pm.enabled", purgepm);
		config.set("purge.pm.regenpurgedplot",purgepmregen);
		config.set("purge.residence.enabled", purgeresidence);
		config.set("purge.residence.regenpurgedresidence",purgeresregenarena);
		config.set("purge.economy.enabled", purgeeconomy);
		config.set("purge.permissions.enabled", purgeperms);
		config.set("purge.dat.enabled", purgedat);

		//crashrestart variables
		config.set("crashrestart.enabled",crashrestartenabled);
		config.set("crashrestart.startdelay",crdelay);
		config.set("crashrestart.scriptpath",crashrestartscriptpath);
		config.set("crashrestart.timeout",crtimeout);
		config.set("crashrestart.juststop", crstop);

		//autorestart variables
		config.set("autorestart.enabled", autorestart);
		config.set("autorestart.broadcast", autorestartBroadcast);
		config.set("autorestart.time",autorestarttime);
		config.set("autorestart.countdown.enabled", autorestartcountdown);
		config.set("autorestart.countdown.broadcastonsecond",autorestartbroadcastonseconds);
		config.set("autorestart.commands", autorestartcommmands);
		config.set("autorestart.scriptpath",autorestartscriptpath);
		config.set("autorestart.juststop", astop);


		//autoconsolecommand variables
		config.set("consolecommand.timemode.enabled", cctimeenabled);
		if (cctimescommands.isEmpty())
		{
			config.createSection("consolecommand.timemode.times");
		}
		for (String cctime : cctimescommands.keySet())
		{
			config.set("consolecommand.timemode.times."+cctime, cctimescommands.get(cctime));
		}
		config.set("consolecommand.intervalmode.enabled", ccintervalenabled);
		if (ccintervalscommands.isEmpty())
		{
			config.createSection("consolecommand.intervalmode.intervals");
		}
		for (int inttime : ccintervalscommands.keySet())
		{
			config.set("consolecommand.intervalmode.intervals."+inttime, ccintervalscommands.get(inttime));
		}

		//worldregen variables
		config.set("worldregen.savewg",worldregensavewg);
		config.set("worldregen.savefactions",worldregensavefactions);
		config.set("worldregen.savegp",worldregensavegp);
		config.set("worldregen.savetowny",worldregensavetowny);

		try {config.save(new File(plugin.constants.getConfigPath()));} catch (IOException ex) {}
	}

}