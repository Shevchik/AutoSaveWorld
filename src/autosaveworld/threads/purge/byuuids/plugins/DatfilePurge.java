package autosaveworld.threads.purge.byuuids.plugins;

import java.io.File;

import org.bukkit.Bukkit;

import autosaveworld.core.logging.MessageLogger;
import autosaveworld.threads.purge.byuuids.ActivePlayersList;

public class DatfilePurge {

	public void doDelPlayerDatFileTask(ActivePlayersList pacheck) {

		MessageLogger.debug("Playre .dat file purge started");

		int deleted = 0;
		String worldfoldername = Bukkit.getWorlds().get(0).getWorldFolder().getAbsolutePath();
		File playersdatfolder = new File(worldfoldername + File.separator + "playerdata"+ File.separator);
		for (File playerfile : playersdatfolder.listFiles()) {
			if (playerfile.getName().endsWith(".dat")) {
				String playeruuid = playerfile.getName().substring(0, playerfile.getName().length() - 4);
				if (!pacheck.isActive(playeruuid)) {
					MessageLogger.debug(playeruuid + " is inactive. Removing dat file");
					playerfile.delete();
					deleted += 1;
				}
			}
		}

		MessageLogger.debug("Player .dat purge finished, deleted "+deleted+" player .dat files");
	}

}
