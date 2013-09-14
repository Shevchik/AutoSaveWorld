package autosaveworld.threads.backup.script;

import java.io.File;
import autosaveworld.config.AutoSaveConfig;
import autosaveworld.core.AutoSaveWorld;

public class ScriptBackup {

	private AutoSaveWorld plugin;
    private AutoSaveConfig config;
    public ScriptBackup(AutoSaveWorld plugin, AutoSaveConfig config)
    {
    	this.plugin = plugin;
    	this.config = config;
    }
    
    
    public void performBackup()
    {
    	String scriptpath = config.scriptbackupscriptpath;
    	if (!scriptpath.isEmpty() && new File(scriptpath).exists())
    	{
    		final Process p;
    		ProcessBuilder pb = new ProcessBuilder();
    		pb.command(new File(config.scriptbackupscriptpath).getPath());
    		try {
    			p = pb.start();
    			OutputThread output = new OutputThread(p);
    			output.start();
    			ErrorThread err = new ErrorThread(p);
    			err.start();
    			p.waitFor();
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    	} else
    	{
    		plugin.debug("Scriptpath is invalid");
    	}
    }
	
	
}
