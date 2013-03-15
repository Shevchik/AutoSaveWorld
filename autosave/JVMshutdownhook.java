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
			Runtime.getRuntime().exec("cmd /c start " + restartscript.getCanonicalPath());
		} else {
			Runtime.getRuntime().exec(restartscript.getCanonicalPath());
		}
		} else {
		System.out.println("[AutoSaveWorld] Startup script not found. Creating new one. This may work strange or not work at all");
		String processname = new File(Bukkit.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getName();
		String memory = Runtime.getRuntime().maxMemory()/1024/1024+"M";
		String encoding = Charset.defaultCharset().toString();
		String runcommand = "java -server -Xmx"+memory+" -XX:+UseBiasedLocking -XX:+AggressiveOpts -XX:+UseStringCache -XX:+UseFastAccessorMethods -Dfile.encoding="+encoding+" -jar "+processname;
		String OS = System.getProperty("os.name").toLowerCase();
		if (OS.contains("win")) {
			Runtime.getRuntime().exec("cmd /c start " + runcommand);
		} else {
			Runtime.getRuntime().exec(new String[]{"/bin/bash","-c","cd "+new File(".").getCanonicalPath()}+" & "+runcommand);
		}
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
