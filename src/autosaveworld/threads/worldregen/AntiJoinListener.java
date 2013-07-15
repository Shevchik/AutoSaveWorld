package autosaveworld.threads.worldregen;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import autosaveworld.config.AutoSaveConfigMSG;
import autosaveworld.core.AutoSaveWorld;

public class AntiJoinListener implements Listener {
	
	private AutoSaveWorld plugin;
	private AutoSaveConfigMSG configmsg;
	
	public AntiJoinListener(AutoSaveWorld plugin, AutoSaveConfigMSG configmsg)
	{
		this.plugin = plugin;
		this.configmsg = configmsg;
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e)
	{
		plugin.kickPlayer(e.getPlayer(),configmsg.messageWorldRegenKick);
	}
}
