package autosaveworld.threads.purge.weregen;

import org.bukkit.World;

import com.sk89q.worldedit.Vector;

public class WorldEditRegeneration {

	private static WorldEditRegenrationInterface instance;
	public static WorldEditRegenrationInterface get() {
		if (instance == null) {
			instance = new BukkitAPIWorldEditRegeneration();
		}
		return instance;
	}
	
	public static interface WorldEditRegenrationInterface {
		public void regenerateRegion(World world, org.bukkit.util.Vector minpoint, org.bukkit.util.Vector maxpoint, RegenOptions options);
		public void regenerateRegion(World world, Vector minpoint, Vector maxpoint, RegenOptions options);
	}

}
