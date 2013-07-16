package autosaveworld.threads.worldregen;

import java.io.File;
import java.io.IOException;

public class WorldRegenJVMshutdownhook extends Thread {
	
	String fldtodelete;
	
	public WorldRegenJVMshutdownhook(String fldtodelete)
	{
		this.fldtodelete = fldtodelete;
		Thread.currentThread().setName("AutoSaveWorld WorldRegenShutdownHook");
	}
	
	public void run()
	{
		//Delete region from world folder
		deleteDirectory(new File(fldtodelete));
		try {
			//create file that indicates that AutoSaveWorld should paste regions from schematics back to map
			new File("plugins/AutoSaveWorld/WorldRegenTemp/shouldpaste").createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	private void deleteDirectory(File file)
	{
	    if(!file.exists())
	      return;
	    if(file.isDirectory())
	    {
	      for(File f : file.listFiles())
	        deleteDirectory(f);
	      file.delete();
	    }
	    else
	    {
	      file.delete();
	    }
	}
	

}
