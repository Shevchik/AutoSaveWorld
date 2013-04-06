package autosave;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;

public class JVMshutdownhook extends Thread {

	private String crashrestartscriptpath = "start.sh"; 
	public void setpath(String path)
	{
		this.crashrestartscriptpath = path;
	}
	
	public void restart()
	{
	try {
		File restartscript = new File(crashrestartscriptpath);
		String OS = System.getProperty("os.name").toLowerCase();
		if (restartscript.exists()) {
			System.out.println("[AutoSaveWorld] Startup script found. Restarting");	
			if (OS.contains("win")) {
				Runtime.getRuntime().exec("cmd /c start " + restartscript.getCanonicalPath());
			} else {
				Runtime.getRuntime().exec(restartscript.getCanonicalPath());
			}
		} else {
			System.out.println("[AutoSaveWorld] Startup script not found. Restarting without it. This may work strange or not work at all");
			//requred info for start script
			File workdir = new File(".").getCanonicalFile();
			String jarfilename = new File(Bukkit.class.getProtectionDomain().getCodeSource().getLocation().getFile()).getName();
			List<String> arguments = ManagementFactory.getRuntimeMXBean().getInputArguments();			

			//start script building
			List<String> execsequence = new ArrayList<String>();
			execsequence.add("java");
			execsequence.addAll(arguments);
			execsequence.add("-jar");
			execsequence.add(jarfilename);
			ProcessBuilder pb = new ProcessBuilder(execsequence);
			pb.directory(workdir);
			pb.start();
			}
	} catch (Exception e)
	{System.out.println("[AutoSaveWorld] Restart failed");
	e.printStackTrace();}
	}
	
	
	public void run()
	{
		restart();
	}
	
}
