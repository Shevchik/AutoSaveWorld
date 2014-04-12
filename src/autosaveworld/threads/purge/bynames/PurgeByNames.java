package autosaveworld.threads.purge.bynames;

import org.bukkit.plugin.PluginManager;

import autosaveworld.config.AutoSaveWorldConfig;
import autosaveworld.core.AutoSaveWorld;
import autosaveworld.threads.purge.byname.plugins.DatfilePurge;
import autosaveworld.threads.purge.byname.plugins.LWCPurge;
import autosaveworld.threads.purge.byname.plugins.MVInvPurge;
import autosaveworld.threads.purge.byname.plugins.MyWarpPurge;
import autosaveworld.threads.purge.byname.plugins.PlotMePurge;
import autosaveworld.threads.purge.byname.plugins.ResidencePurge;
import autosaveworld.threads.purge.byname.plugins.VaultPurge;
import autosaveworld.threads.purge.byname.plugins.WGPurge;

public class PurgeByNames {
	
	private AutoSaveWorld plugin = null;
	private AutoSaveWorldConfig config;
	public PurgeByNames(AutoSaveWorld plugin, AutoSaveWorldConfig config) {
		this.plugin = plugin;
		this.config = config;
	}
	
	public void startPurge() {
		plugin.debug("Gathering active players list");
		ActivePlayersList aplist = new ActivePlayersList(plugin, config);
		aplist.gatherActivePlayersList(config.purgeAwayTime * 1000);
		plugin.debug("Found "+aplist.getActivePlayersCount()+" active players");

		PluginManager pm = plugin.getServer().getPluginManager();

		if ((pm.getPlugin("WorldGuard") != null) && config.purgewg) {
			plugin.debug("WG found, purging");
			try {
				new WGPurge(plugin).doWGPurgeTask(aplist, config.purgewgregenrg, config.purgewgnoregenoverlap);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if ((pm.getPlugin("LWC") != null) && config.purgelwc) {
			plugin.debug("LWC found, purging");
			try {
				new LWCPurge(plugin).doLWCPurgeTask(aplist, config.purgelwcdelprotectedblocks);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if ((pm.getPlugin("Multiverse-Inventories") !=null) && config.purgemvinv) {
			plugin.debug("Multiverse-Inventories found, purging");
			try {
				new MVInvPurge(plugin).doMVInvPurgeTask(aplist);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if ((pm.getPlugin("PlotMe") !=null) && config.purgepm) {
			plugin.debug("PlotMe found, purging");
			try {
				new PlotMePurge(plugin).doPlotMePurgeTask(aplist, config.purgepmregen);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if ((pm.getPlugin("Residence") !=null) && config.purgeresidence) {
			plugin.debug("Residence found, purging");
			try {
				new ResidencePurge(plugin).doResidencePurgeTask(aplist, config.purgeresregenarena);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (pm.getPlugin("Vault") != null) {
			VaultPurge vp = new VaultPurge(plugin);
			if (config.purgeeconomy) {
				plugin.debug("Vault found, purging economy");
				vp.doEconomyPurgeTask(aplist);
			}
			if (config.purgeperms) {
				plugin.debug("Vault found, purging permissions");
				vp.doPermissionsPurgeTask(aplist);
			}
		}

		if (pm.getPlugin("MyWarp") != null && config.purgemywarp) {
			plugin.debug("MyWarp found, purging");
			try {
				new MyWarpPurge(plugin).doMyWarpPurgeTask(aplist);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		plugin.debug("Purging player .dat files");
		if (config.purgedat) {
			try {
				new DatfilePurge(plugin).doDelPlayerDatFileTask(aplist);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
