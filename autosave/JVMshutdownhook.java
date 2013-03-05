package autosave;

import java.io.File;

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
		String OS = System.getProperty("os.name").toLowerCase();
		if (OS.contains("win")) {
			Runtime.getRuntime().exec("cmd /c start " + restartscript.getPath());
		} else {
			Runtime.getRuntime().exec(restartscript.getPath());
		}
		} else {
		System.out.println("[AutoSaveWorld] Startup script not found. CrashRestart failed");	
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
