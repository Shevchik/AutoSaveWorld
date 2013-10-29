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

package autosaveworld.threads.worldregen;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.schematic.SchematicFormat;

import autosaveworld.config.AutoSaveConfig;
import autosaveworld.config.AutoSaveConfigMSG;
import autosaveworld.core.AutoSaveWorld;
import autosaveworld.threads.worldregen.factions.FactionsCopy;
import autosaveworld.threads.worldregen.griefprevention.GPCopy;
import autosaveworld.threads.worldregen.wg.WorldGuardCopy;

public class WorldRegenCopyThread extends Thread {

	private AutoSaveWorld plugin = null;
	private AutoSaveConfig config;
	private AutoSaveConfigMSG configmsg;
	public WorldRegenCopyThread(AutoSaveWorld plugin, AutoSaveConfig config, AutoSaveConfigMSG configmsg)
	{
		this.plugin = plugin;
		this.config = config;
		this.configmsg = configmsg;
	}
	
	// Allows for the thread to naturally exit if value is false
	public void stopThread() {
		this.run = false;
	}
	
	public void startworldregen(String worldname) {
		this.worldtoregen = worldname;
		plugin.worldregenInProcess = true;
		doregen = true;
	}
		
	
	private String worldtoregen = "";
	private int taskid;
	private volatile boolean run = true;
	private boolean doregen = false;
	public void run()
	{
		plugin.debug("WorldRegenThread Started");
		Thread.currentThread().setName("AutoSaveWorld WorldRegenCopyThread");
		
		while (run)
		{
			if (doregen)
			{
				try {
				doWorldRegen();
				} catch (Exception e) {
					e.printStackTrace();
				}
				doregen = false;
				run = false;
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		plugin.debug("Graceful quit of WorldRegenThread");
	}
	
	
	private void doWorldRegen() throws Exception
	{
		if (plugin.backupInProgress) {
			plugin.warn("AutoBackup is in process. WorldRegen cancelled.");
			return;
		}
		if (plugin.purgeInProgress) {
			plugin.warn("AutoPurge is in process. WorldRegen cancelled.");
			return;
		}
		
		final World wtoregen = Bukkit.getWorld(worldtoregen);
		
		FileConfiguration cfg = new YamlConfiguration();
		cfg.set("wname", worldtoregen);
		cfg.save(new File(plugin.constants.getWorldnameFile()));
		
		//kick all player and deny them from join
		AntiJoinListener ajl = new AntiJoinListener(plugin,configmsg);
		Bukkit.getPluginManager().registerEvents(ajl, plugin);
		taskid = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
		{
			public void run()
			{
				for (Player p : Bukkit.getOnlinePlayers())
				{
					plugin.kickPlayer(p,configmsg.messageWorldRegenKick);
				}
			}
		});
		while (Bukkit.getScheduler().isCurrentlyRunning(taskid) || Bukkit.getScheduler().isQueued(taskid))
		{
				Thread.sleep(1000);
		}
		
		plugin.debug("Saving buildings");
		
		//save WorldGuard buildings
		if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null && config.worldregensavewg)
		{
			new WorldGuardCopy(plugin, this, worldtoregen).copyAllToSchematics();
		}
		
		//save Factions homes
		if (Bukkit.getPluginManager().getPlugin("Factions") != null && config.worldregensavefactions)
		{
			new FactionsCopy(plugin, this, worldtoregen).copyAllToSchematics();
		}
		
		//save GriefPrevention claims
		if (Bukkit.getPluginManager().getPlugin("GriefPrevention") != null && config.worldregensavegp)
		{
			new GPCopy(plugin, this, worldtoregen).copyAllToSchematics();
		}
		
		plugin.debug("Saving finished");
		
		//Shutdown server and delegate world removal to JVMShutdownHook
		plugin.debug("Deleting map and restarting server");
		plugin.JVMsh.setPath(config.autorestartscriptpath);
		WorldRegenJVMshutdownhook wrsh = new WorldRegenJVMshutdownhook(plugin.JVMsh, wtoregen.getWorldFolder().getCanonicalPath(), plugin.constants.getShouldpasteFile());
		Runtime.getRuntime().addShutdownHook(wrsh);
		plugin.getServer().shutdown();
	}

	private int ststaskid;
    private final SchematicFormat format = SchematicFormat.getFormats().iterator().next();
	public void saveToSchematic(final String schematic, final World world, final Vector bvmin, final Vector bvmax)
	{
		Runnable copypaste = new Runnable() 
		{
			public void run()
			{
				try {
					//copy to clipboard
					EditSession es = new EditSession(new BukkitWorld(world),Integer.MAX_VALUE);
					CuboidClipboard clipboard = new CuboidClipboard(
							bvmax.subtract(bvmin).add(new Vector(1, 1, 1)),
							bvmin, bvmin.subtract(bvmax)
					);
					clipboard.copy(es);
					//save to schematic
					File f= new File(schematic);
					format.save(clipboard, f);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		ststaskid = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, copypaste);
		while (Bukkit.getScheduler().isCurrentlyRunning(ststaskid) || Bukkit.getScheduler().isQueued(ststaskid))
		{
			try {Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace();}
		}
	}
	
	
}
