package autosave;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class AutoPurgeThread extends Thread {
	
	protected final Logger log = Logger.getLogger("Minecraft");
	private AutoSave plugin = null;
	private AutoSaveConfig config;
	private AutoSaveConfigMSG configmsg;
	private boolean run = true;
	private boolean command = false;
	AutoPurgeThread(AutoSave plugin, AutoSaveConfig config, AutoSaveConfigMSG configmsg) {
		this.plugin = plugin;
		this.config = config;
		this.configmsg = configmsg;
	}

	// Allows for the thread to naturally exit if value is false
	public void setRun(boolean run) {
		this.run = run;
	}
	private int runnow;
	public void startsave()
	{
	command = true;
	runnow = config.purgeInterval;
	}
	
	
	// The code to run...weee
	public void run() {
		if (config == null) {
			return;
		}

		log.info(String
				.format("[%s] AutoSaveThread Started: Interval is %d seconds, Warn Times are %s",
						plugin.getDescription().getName(), config.purgeInterval,
						Generic.join(",", config.varWarnTimes)));
		while (run) {
			// Prevent AutoPurge from never sleeping
			// If interval is 0, sleep for 5 seconds and skip saving
			if(config.varInterval == 0) {
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
							log.info(String.format("[%s] Graceful quit of AutoPurgeThread", plugin.getDescription().getName()));
						}
						return;
					}
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					log.info("Could not sleep!");
				}
			}
			
			 if (config.purgeEnabled||command) performPurge();


		}
	}
	
	public void performPurge() {
		WGpurge();
		command = false;
	}
	
	public void WGpurge() {
		int awaytime = config.purgeAwayTime;
		WorldGuardPlugin wg = (WorldGuardPlugin) plugin.getServer().getPluginManager().getPlugin("WorldGuard");
		for(World w : Bukkit.getWorlds()) {
		RegionManager m = wg.getRegionManager(w);
		for(ProtectedRegion rg : m.getRegions().values()) {
			DefaultDomain dd = rg.getOwners();
			ArrayList<String> pltodelete = new ArrayList<String>();
			for (String checkPlayer : dd.getPlayers()) {
				if (!Bukkit.getOfflinePlayer(checkPlayer).hasPlayedBefore()) {
				Player pl = Bukkit.getOfflinePlayer(checkPlayer).getPlayer();
				if (System.currentTimeMillis() - pl.getLastPlayed() >= awaytime)
				{
					pltodelete.add(checkPlayer);
				}
				}
			}
			if (dd.getPlayers().size() <= pltodelete.size()) {
				m.removeRegion(rg.getId());
			} else {
				if (pltodelete.size() != 0) {
					for (String plrem : pltodelete) {
					dd.removePlayer(plrem);}
					rg.setOwners(dd);
				}
				
			}
			try {m.save();} catch (ProtectionDatabaseException e) {e.printStackTrace();}
			
			}
		}
	}
	
}
