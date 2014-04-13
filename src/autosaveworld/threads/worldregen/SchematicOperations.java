package autosaveworld.threads.worldregen;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.World;

import autosaveworld.core.AutoSaveWorld;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalEntity;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.schematic.SchematicFormat;

public class SchematicOperations {

	private AutoSaveWorld plugin;
	public SchematicOperations(AutoSaveWorld plugin) {
		this.plugin = plugin;
	}

	public void saveToSchematic(final String schematic, final World world, final Vector bvmin, final Vector bvmax) {
		Runnable copypaste = new Runnable() {
			@Override
			public void run() {
				try {
					//create clipboard
					BukkitWorld bw = new BukkitWorld(world);
					EditSession es = new EditSession(bw, Integer.MAX_VALUE);
					CuboidClipboard clipboard = new CuboidClipboard(
						bvmax.subtract(bvmin).add(new Vector(1, 1, 1)),
						bvmin, 
						bvmin.subtract(bvmax)
					);
					Region region = new CuboidRegion(bw, bvmin, bvmax);
					es.setFastMode(true);
					//copy blocks
					clipboard.copy(es);
					//copy entities
		            LocalEntity[] entities = bw.getEntities(region);
		            for (LocalEntity entity : entities) {
		                clipboard.storeEntity(entity);
		            }
					//save to schematic
					File f = new File(schematic);
					SchematicFormat.getFormats().iterator().next().save(clipboard, f);
				} catch (IOException | DataException e) {
					e.printStackTrace();
				}
			}
		};
		int ststaskid = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, copypaste);
		while (Bukkit.getScheduler().isCurrentlyRunning(ststaskid) || Bukkit.getScheduler().isQueued(ststaskid)) {
			try {Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace();}
		}
	}

	public void pasteFromSchematic(final String schematic, final World world) {
		try {
			//load from schematic to clipboard
			final EditSession es = new EditSession(new BukkitWorld(world),Integer.MAX_VALUE);
			es.setFastMode(true);
			es.enableQueue();
			File f = new File(schematic);
			final CuboidClipboard cc = SchematicFormat.getFormat(f).load(f);
			//get schematic coords
			final Vector size = cc.getSize();
			final Vector origin = cc.getOrigin();
			Runnable genchunks = new Runnable() {
				@Override
				public void run() {
					//generate chunks at schematic position and 3 chunk radius nearby
					for (int x = -16*3; x < size.getBlockX() + 16*3; x+=16) {
						for (int z = -16*3; z < size.getBlockZ() + 16*3; z+=16) {
							//getChunkAt automatically loads chunk
							world.getChunkAt(origin.getBlockX()+x, origin.getBlockZ()+z).load();
						}
					}
				}
			};
			int pfstaskid = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, genchunks);
			while (Bukkit.getScheduler().isCurrentlyRunning(pfstaskid) || Bukkit.getScheduler().isQueued(pfstaskid)) {
				try {Thread.sleep(100);} catch (InterruptedException e) {}
			}
			Runnable paste = new Runnable() {
				@Override
				public void run() {
					//paste blocks
					for (int x = 0; x < size.getBlockX(); ++x) {
						for (int y = 0; y < size.getBlockY(); ++y) {
							for (int z = 0; z < size.getBlockZ(); ++z) {
								Vector blockpos = new Vector(x, y, z);
								final BaseBlock block = cc.getBlock(blockpos);

								if (block == null) {
									continue;
								}

								try {
									es.smartSetBlock(new Vector(x, y, z).add(origin), block);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}
					}
					es.flushQueue();
					//paste entities
					try {
						cc.pasteEntities(origin);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};
			pfstaskid = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, paste);
			while (Bukkit.getScheduler().isCurrentlyRunning(pfstaskid) || Bukkit.getScheduler().isQueued(pfstaskid)) {
				try {Thread.sleep(100);} catch (InterruptedException e) {}
			}
		} catch (IOException | DataException e) {
			e.printStackTrace();
		}
	}

}
