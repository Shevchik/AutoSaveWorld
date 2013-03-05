package autosave;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class JVMshutdownhook extends Thread {

	public void run()
	{
		
		FileConfiguration restartfile = YamlConfiguration.loadConfiguration(new File("plugins/ASWrestartmarker"));
		restartfile.set("restart", 0);
		try {restartfile.save(new File("plugins/ASWrestartmarker"));} catch (IOException e) {e.printStackTrace();}
	}
	
}
