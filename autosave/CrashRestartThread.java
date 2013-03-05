package autosave;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;

public class CrashRestartThread extends Thread{

	private AutoSave plugin;
	private AutoSaveConfig config;
	private boolean run = true;
	private boolean test = false;
	private boolean stopped = false;
	public volatile boolean restart = false;
	protected final Logger log = Logger.getLogger("Minecraft");
	private long syncticktime = 0;
	
	CrashRestartThread(AutoSave plugin,AutoSaveConfig config)
	{
		this.plugin = plugin;
		this.config = config;
	}
	
	public void stopthread()
	{
		this.run = false;
	}
	
	public void test()
	{
		this.test = true;
	}
	public void serverstop()
	{
		this.stopped = true;
	}
	
	public void run()
	{	
		log.info("[AutoSaveWorld] CrashRestartThread started");
		plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			public void run() {
				syncticktime = System.currentTimeMillis();
				if (config.crdebug) {
				plugin.debug("CrashRestartThread SyncTicktime: "+syncticktime);
				}
			}	
		}, 0, 20);
		while (run)
		{
			if (!stopped) {
			long diff = System.currentTimeMillis() - syncticktime;
			if (test) {diff = config.crtimeout*1000L + 1;}
			if (syncticktime !=0 && (diff >= (config.crtimeout*1000L)))
			{if (config.crashrestartenabled) {
				restart = true;
				ConsoleCommandSender sender = Bukkit.getConsoleSender();
				plugin.getServer().dispatchCommand(sender, "stop");
				run = false;
				} else {
				plugin.debug("Crash detected by CrashRestartThread, but crashrestart is disabled");
				run = false;
				}
			}
			if (config.crdebug) {
				plugin.debug("CrashRestartThread ASyncTicktime: "+System.currentTimeMillis());
				plugin.debug("CrashRestartThread diff: "+diff);
				}
			try {Thread.sleep(999);} catch (InterruptedException e) {e.printStackTrace();}
			}
		}
		}
    }

