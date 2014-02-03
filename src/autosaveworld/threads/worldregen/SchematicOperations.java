package autosaveworld.threads.worldregen;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.World;

import autosaveworld.core.AutoSaveWorld;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.schematic.SchematicFormat;

public class SchematicOperations {

	private AutoSaveWorld plugin;
	public SchematicOperations(AutoSaveWorld plugin)
	{
		this.plugin = plugin;
	}

	private int ststaskid;
	public void saveToSchematic(final String schematic, final World world, final Vector bvmin, final Vector bvmax)
	{
		Runnable copypaste = new Runnable()
		{
			@Override
			public void run()
			{
				int tries = 0;
				boolean success = false;
				while (tries < 3 && !success)
				{
					try {
						tryCopy(schematic, world, bvmin, bvmax);
						success = true;
					} catch (Exception e) {
						e.printStackTrace();
						plugin.debug("Schematic copy failed, trying again");
					}
					tries++;
				}
				if (success)
				{
					plugin.debug("Copied schematic in "+tries+" tries");
				} else
				{
					plugin.debug("Schematic copy failed 3 times, giving up");
				}
			}
		};
		ststaskid = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, copypaste);
		while (Bukkit.getScheduler().isCurrentlyRunning(ststaskid) || Bukkit.getScheduler().isQueued(ststaskid))
		{
			try {Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace();}
		}
	}
	private void tryCopy(final String schematic, final World world, final Vector bvmin, final Vector bvmax) throws IOException, DataException
	{
		//copy to clipboard
		EditSession es = new EditSession(new BukkitWorld(world),Integer.MAX_VALUE);
		es.setFastMode(true);
		CuboidClipboard clipboard = new CuboidClipboard(
				bvmax.subtract(bvmin).add(new Vector(1, 1, 1)),
				bvmin, bvmin.subtract(bvmax)
		);
		clipboard.copy(es);
		//save to schematic
		File f= new File(schematic);
		SchematicFormat.getFormats().iterator().next().save(clipboard, f);
	}





	private int pfstaskid;
	public void pasteFromSchematics(final String shematic, final World world)
	{
		Runnable copypaste = new Runnable()
		{
			@Override
			public void run()
			{
				int tries = 0;
				boolean success = false;
				while (tries < 3 && !success)
				{
					try {
						tryPaste(shematic,world);
						success = true;
					} catch (Exception e) {
						e.printStackTrace();
						plugin.debug("Schematic paste failed, trying again");
					}
					tries++;
				}
				if (success)
				{
					plugin.debug("Pasted schematic in "+tries+" tries");
				} else
				{
					plugin.debug("Schematic paste failed 3 times, giving up");
				}
			}
		};
		pfstaskid = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, copypaste);
		while (Bukkit.getScheduler().isCurrentlyRunning(pfstaskid) || Bukkit.getScheduler().isQueued(pfstaskid)) {
			try {Thread.sleep(100);} catch (InterruptedException e){e.printStackTrace();}
		}
	}
	private void tryPaste(final String shematic, final World world) throws IOException, DataException, MaxChangedBlocksException
	{
		//load from schematic to clipboard
		EditSession es = new EditSession(new BukkitWorld(world),Integer.MAX_VALUE);
		es.setFastMode(true);
		File f = new File(shematic);
		CuboidClipboard cc = SchematicFormat.getFormat(f).load(f);
		//paste clipboard at origin
		Vector size = cc.getSize();
		Vector origin = cc.getOrigin();
		for (int x = 0; x < size.getBlockX(); ++x) 
		{
			for (int y = 0; y < size.getBlockY(); ++y) 
			{
				for (int z = 0; z < size.getBlockZ(); ++z) 
				{
					Vector blockpos = new Vector(x, y, z);
					final BaseBlock block = cc.getBlock(blockpos);

					if (block == null) 
					{
						continue;
					}

					try {
						es.setBlock(new Vector(x, y, z).add(origin), block);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}


}
