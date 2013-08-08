package autosaveworld.threads.restart;

import java.util.HashSet;

//autorestart shutdown hooks uses this class to check if no threads are requring to wait.
public class RestartWaiter {

	//if this hashset is empty then autorestart shutdown hook will start restarting process.
	//if it is not empty autorestart will wait until it becomes empty
	private volatile static HashSet<String> reasons = new HashSet<String>();
	
	public static boolean canRestartNow()
	{
		boolean can = true;
		if (!reasons.isEmpty()) {can = false;}
		return can;
	}
	
	public static HashSet<String> getReasons()
	{
		return reasons;
	}
	
	public static void addReason(String reason)
	{
		reasons.add(reason);
	}
	
	public static void removeReason(String reason)
	{
		reasons.remove(reason);
	}
	
}
