package autosaveworld.threads.purge.weregen;

import java.util.LinkedList;

import org.bukkit.World;
import org.bukkit.craftbukkit.v1_7_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R3.util.LongHash;

import autosaveworld.threads.purge.weregen.UtilClasses.BlockToPlaceBack;
import autosaveworld.threads.purge.weregen.UtilClasses.PlaceBackStage;
import autosaveworld.threads.purge.weregen.WorldEditRegeneration.WorldEditRegenrationInterface;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldguard.bukkit.BukkitUtil;

public class NMS179WorldEditRegeneration implements WorldEditRegenrationInterface {

	public void regenerateRegion(World world, org.bukkit.util.Vector minpoint, org.bukkit.util.Vector maxpoint, RegenOptions options) {
		Vector minbpoint = BukkitUtil.toVector(minpoint);
		Vector maxbpoint = BukkitUtil.toVector(maxpoint);
		regenerateRegion(world, minbpoint, maxbpoint, options);
	}

	@SuppressWarnings("deprecation")
	public void regenerateRegion(World world, Vector minpoint, Vector maxpoint, RegenOptions options) {
		BukkitWorld bw = new BukkitWorld(world);
		EditSession es = new EditSession(bw, Integer.MAX_VALUE);
		es.setFastMode(true);
		int maxy = bw.getMaxY() + 1;
		Region region = new CuboidRegion(bw, minpoint, maxpoint);
		LinkedList<BlockToPlaceBack> placeBackQueue = new LinkedList<BlockToPlaceBack>();

		//get all new chunk data
		for (Vector2D chunk : region.getChunks()) {
			net.minecraft.server.v1_7_R3.WorldServer nmsWorld = ((CraftWorld)world).getHandle();
			int cx = chunk.getBlockX();
			int cz = chunk.getBlockZ();
			//load chunk to ensure that it is in the map
			world.getChunkAt(cx, cz).load();
			//save old chunk
			net.minecraft.server.v1_7_R3.Chunk oldnsmchunk = nmsWorld.chunkProviderServer.chunks.get(LongHash.toLong(cx, cz));
			//generate nms chunk
			net.minecraft.server.v1_7_R3.Chunk nmschunk = null;
			if (nmsWorld.chunkProviderServer.chunkProvider == null) {
				nmschunk = nmsWorld.chunkProviderServer.emptyChunk;
			} else {
				nmschunk = nmsWorld.chunkProviderServer.chunkProvider.getOrCreateChunk(cx, cz);
			}
			//put new chunk to map so worldedit can copy blocks from it
			nmsWorld.chunkProviderServer.chunks.put(LongHash.toLong(cx, cz), nmschunk);
		    //save all generated data inside the region
			Vector min = new Vector(chunk.getBlockX() * 16, 0, chunk.getBlockZ() * 16);
			for (int x = 0; x < 16; ++x) {
				for (int y = 0; y < maxy; ++y) {
					for (int z = 0; z < 16; ++z) {
						Vector pt = min.add(x, y, z);
						if (region.contains(pt)) {
							placeBackQueue.add(new BlockToPlaceBack(pt, es.getBlock(pt)));
						}
					}
				}
			}
			//put old chunk to map
			nmsWorld.chunkProviderServer.chunks.put(LongHash.toLong(cx, cz), oldnsmchunk);
		}

		//set all blocks that were inside the region back
		for (PlaceBackStage stage : UtilClasses.placeBackStages) {
			stage.processBlockPlaceBack(world, es, placeBackQueue);
		}
	}

}
