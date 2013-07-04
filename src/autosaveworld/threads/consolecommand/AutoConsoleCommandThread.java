package autosaveworld.threads.consolecommand;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;

import autosaveworld.config.AutoSaveConfig;
import autosaveworld.core.AutoSaveWorld;

public class AutoConsoleCommandThread extends Thread {

	private AutoSaveWorld plugin = null;
	private AutoSaveConfig config;
	private boolean run = true;
	protected final Logger log = Bukkit.getLogger();
	SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
	SimpleDateFormat msdf = new SimpleDateFormat("mm");

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
					plugin.debug("Executing console commands (timesmode)");
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
					plugin.debug("Executing console commands (intervalmode)");
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
		if (Integer.valueOf(msdf.format(System.currentTimeMillis())) != minute)
		{
			timesexecuted = false;
		}
	}
	private void enabletimeslock()
	{
		minute = Integer.valueOf(msdf.format(System.currentTimeMillis()));
		timesexecuted = true;
	}
	
	private String getCurTime()
	{
		String curtime = sdf.format(System.currentTimeMillis());
		return curtime;
	}
	
	//intervalmode checks
	private long lastintervalexecute = System.currentTimeMillis()/1000;
	
	
	
}
