package autosaveworld.threads.backup.script;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ErrorThread extends Thread {

	private Process p;
	public ErrorThread(Process p)
	{
		this.p = p;
	}
	

	public void run()
	{
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
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
