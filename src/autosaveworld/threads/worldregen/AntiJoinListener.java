package autosaveworld.threads.worldregen;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import autosaveworld.config.AutoSaveConfigMSG;

public class AntiJoinListener implements Listener {
	
	private AutoSaveConfigMSG configmsg;
	
	public AntiJoinListener(AutoSaveConfigMSG configmsg)
	{
		this.configmsg = configmsg;
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e)
	{
		e.getPlayer().kickPlayer(configmsg.messageWorldRegenKick);
	}
}
