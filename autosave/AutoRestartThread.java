package autosave;

import java.util.Calendar;
import java.util.logging.Logger;

import org.bukkit.Bukkit;


public class AutoRestartThread  extends Thread{
	private AutoSave plugin;
	private AutoSaveConfig config;
	private boolean run = true;
	protected final Logger log = Bukkit.getLogger();

	AutoRestartThread(AutoSave plugin,AutoSaveConfig config)
	{
		this.plugin = plugin;
		this.config = config;
	}
	
	public void stopthread()
	{
		this.run = false;
	}
	
	public void run()
	{	
		log.info("[AutoSaveWorld] AutoRestartThread started");
		Thread.currentThread().setName("AutoSaveWorld_AutoRestartThread");
		
		//check if we just restarted (server can restart faster than 1 minute, without this check, AutoRestartThread will stop working after restart)
		if ((Integer.valueOf(config.autorestarttime.substring(0, 2)) == Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) &&
				(Integer.valueOf(config.autorestarttime.substring(3)) == Calendar.getInstance().get(Calendar.MINUTE)))
			{
			//sleep for 1 minute
			try {Thread.sleep(61000);} catch (InterruptedException e) {e.printStackTrace();}
			}
		
		while (run)
		{
		//i know that this can be done using a java.util.timer, but i need a way to reload timer time
			if (config.autorestart)
			{
			int rhours = Integer.valueOf(config.autorestarttime.substring(0, 2));
			int rminutes = Integer.valueOf(config.autorestarttime.substring(3));
			Calendar cal = Calendar.getInstance();
			int curhours = cal.get(Calendar.HOUR_OF_DAY);
			int curminutes = cal.get(Calendar.MINUTE);
			 if (curhours == rhours && curminutes == rminutes )
			 {
				 log.info("[AutoSaveWorld] AutoRestarting server");
				plugin.getServer().dispatchCommand(Bukkit.getConsoleSender(), "stop");
				if (!config.astop) {
				plugin.JVMsh.setpath(config.autorestartscriptpath);
				Runtime.getRuntime().addShutdownHook(plugin.JVMsh); 
				run = false;
				}
			 }
			}
		try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
		}
		if (config.varDebug) {
			log.info(String.format("[%s] Graceful quit of AutoRestartThread", plugin.getDescription().getName()));
		}
		}
}
