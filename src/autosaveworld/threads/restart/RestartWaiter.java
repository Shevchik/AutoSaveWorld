package autosaveworld.threads.restart;

public class RestartWaiter {

	private static int waittorestart = 0;

	public static boolean shouldWait() {
		return waittorestart != 0;
	}

	public static void incrementWait() {
		waittorestart++;
	}

	public static void decrementWait() {
		if (waittorestart > 0) {
			waittorestart--;
		}
	}

}
