package autosave;

import java.io.File;
import java.nio.charset.Charset;

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
		if (restartscript.exists()) {
		System.out.println("[AutoSaveWorld] Startup script found. Restarting");	
		String OS = System.getProperty("os.name").toLowerCase();
		if (OS.contains("win")) {
			Runtime.getRuntime().exec("cmd /c start " + restartscript.getPath());
		} else {
			Runtime.getRuntime().exec(restartscript.getPath());
		}
		} else {
		System.out.println("[AutoSaveWorld] Startup script not found. Trying to restart without it. This may work strange or not work at all");
		String processname = new File(Bukkit.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getName();
		String memory = Runtime.getRuntime().maxMemory()/1024/1024+"M";
		String encoding = Charset.defaultCharset().toString();
		String runcommand = "java -server -Xmx"+memory+" -XX:+UseBiasedLocking -XX:+AggressiveOpts -XX:+UseStringCache -XX:+UseFastAccessorMethods -Dfile.encoding="+encoding+" -jar "+processname;
		System.out.println("Constructed startserver command: "+runcommand);
		String OS = System.getProperty("os.name").toLowerCase();
		if (OS.contains("win")) {
			Runtime.getRuntime().exec("cmd /c start "+runcommand);
		} else {
			Runtime.getRuntime().exec(runcommand);
		}
		}
	} catch (Exception e)
	{System.out.println("[AutoSaveWorld] CrashRestart failed");
	e.printStackTrace();}
	}
	
	
	public void run()
	{
		restart();
	}
	
}
