package autosaveworld.threads.backup.script;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class OutputThread extends Thread{
	
	private Process p;
	public OutputThread(Process p)
	{
		this.p = p;
	}
	

	public void run()
	{
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			while((line=br.readLine()) != null)
			{
				System.out.println(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
