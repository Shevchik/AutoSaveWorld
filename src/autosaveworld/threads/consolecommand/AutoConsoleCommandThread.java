package autosaveworld.threads.consolecommand;

import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;

import autosaveworld.config.AutoSaveConfig;
import autosaveworld.core.AutoSaveWorld;

public class AutoConsoleCommandThread extends Thread {

	protected final Logger log = Bukkit.getLogger();
	private AutoSaveWorld plugin = null;
	private AutoSaveConfig config;
	private volatile boolean run = true;

	public AutoConsoleCommandThread(AutoSaveWorld plugin, AutoSaveConfig config) {
		this.plugin = plugin;
		this.config = config;
	}
	

	private void executeCommands(final List<String> commands)
	{
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
		{
			public void run()
			{
				ConsoleCommandSender csender = Bukkit.getConsoleSender();
				for (String command : commands)
				{
					Bukkit.dispatchCommand(csender, command);
				}
			}
		});
	}
	
	public void stopThread()
	{
		this.run = false;
	}
	
	public void run() {

		log.info(String.format("[%s] AutoConsoleCommandThread Started",
						plugin.getDescription().getName()
					)
				);
		Thread.currentThread().setName("AutoSaveWorld AutoConsoleCommandThread");

		
		while (run) {
			
			//handle times mode
			if (config.cctimeenabled) {
				checktimeslock();
				String ctime = getCurTime();
				if (!timesexecuted && config.cctimetimes.contains(ctime))
				{
					plugin.debug("sent commands to console to execute (timesmode)");
					enabletimeslock();
					executeCommands(config.cctimescommands.get(ctime));
				}
			}
			
			//handle interval mode
			if (config.ccintervalenabled)
			{
				long cseconds = System.currentTimeMillis()/1000;
				if (cseconds - lastintervalexecute >= config.ccintervalinterval)
				{
					plugin.debug("sent commands to console to execute (intervalmode)");
					lastintervalexecute = cseconds;
					executeCommands(config.ccintervalcommands);
				}
			}
			
			//sleep for a second
			try {Thread.sleep(1000);} catch (InterruptedException e) {}
		}
		
		//message before disabling thread
		if (config.varDebug) {log.info("[AutoSaveWorld] Graceful quit of AutoConsoleCommandThread");}
	}
	
	
	//timesmode checks
	private int minute = 0;
	private boolean timesexecuted = false;
	private void checktimeslock()
	{
		if (Calendar.getInstance().get(Calendar.MINUTE) != minute)
		{
			timesexecuted = false;
		}
	}
	private void enabletimeslock()
	{
		minute = Calendar.getInstance().get(Calendar.MINUTE);
		timesexecuted = true;
	}
	
	private String getCurTime()
	{
		Calendar cal = Calendar.getInstance();
		String curtime = 	cal.get(Calendar.HOUR_OF_DAY)+ ":"+  cal.get(Calendar.MINUTE);
		return curtime;
	}
	
	//intervalmode checks
	private long lastintervalexecute = System.currentTimeMillis()/1000;
	
	
	
}
