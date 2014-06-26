package autosaveworld.threads.purge.weregen;

import org.bukkit.Bukkit;
import org.bukkit.World;

import autosaveworld.core.logging.MessageLogger;

import com.sk89q.worldedit.Vector;

public class WorldEditRegeneration {

	private static WorldEditRegenrationInterface instance;
	public static WorldEditRegenrationInterface get() {
		if (instance == null) {
			String packageName = Bukkit.getServer().getClass().getPackage().getName();
			String nmspackageversion = packageName.substring(packageName.lastIndexOf('.') + 1);
			switch (nmspackageversion) {
				case "v1_7_R3": {
					instance = new NMS179WorldEditRegeneration();
					MessageLogger.debug("Using NMS179 WorldEdit regeneration");
				}
			}
			if (instance == null) {
				instance = new BukkitAPIWorldEditRegeneration();
				MessageLogger.debug("Using BukkitAPI WorldEdit regeneration");
			}
		}
		return instance;
	}

	public static interface WorldEditRegenrationInterface {
		public void regenerateRegion(World world, org.bukkit.util.Vector minpoint, org.bukkit.util.Vector maxpoint, RegenOptions options);
		public void regenerateRegion(World world, Vector minpoint, Vector maxpoint, RegenOptions options);
	}

}
