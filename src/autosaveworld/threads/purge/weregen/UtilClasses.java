package autosaveworld.threads.purge.weregen;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;

public class UtilClasses {

	public static class BlockToPlaceBack {

		private Vector position;

		private BaseBlock block;

		public BlockToPlaceBack(Vector position, BaseBlock block) {
			this.position = position;
			this.block = block;
		}

		public Vector getPosition() {
			return position;
		}

		public BaseBlock getBlock() {
			return block;
		}

	}

	public static class ItemSpawnListener implements Listener {

		@EventHandler
		public void onItemSpawn(ItemSpawnEvent event) {
			event.setCancelled(true);
		}

	}

}
