package autosaveworld.features.restart;

import java.util.concurrent.Phaser;

public class RestartWaiter {

	public static void init() {
	}

	private static final Phaser phaser = new Phaser();

	public static void await() {
		phaser.register();
		phaser.arriveAndAwaitAdvance();
	}

	public static void incrementWait() {
		phaser.register();
	}

	public static void decrementWait() {
		phaser.arrive();
	}

}
