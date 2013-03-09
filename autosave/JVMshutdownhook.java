package autosave;

import java.io.File;
import java.io.PrintWriter;
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
			File startupscript = new File("ASWstartupscript.bat");
			PrintWriter out = new PrintWriter(startupscript.getAbsoluteFile());
			out.print(runcommand);
			out.close();
			Runtime.getRuntime().exec("cmd /c start " + startupscript.getCanonicalPath());
		} else {
			File startupscript = new File("ASWstartupscript.sh");
			PrintWriter out = new PrintWriter(startupscript.getAbsoluteFile());
			out.println("#!/bin/sh");
			out.println("BINDIR=$(dirname '$(readlink -fn '$0')')");
			out.println("cd '$BINDIR'");
			out.print(runcommand);
			out.close();
			startupscript.setExecutable(true);
			Runtime.getRuntime().exec(startupscript.getCanonicalPath());
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
