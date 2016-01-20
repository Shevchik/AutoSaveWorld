package autosaveworld.threads.restart;

import java.util.concurrent.locks.AbstractQueuedSynchronizer;

public class RestartWaiter {

	public static void init() {
	}

	private static final CountLatch latch = new CountLatch();

	public static void await() {
		try {
			latch.await();
		} catch (InterruptedException e) {
		}
	}

	public static void incrementWait() {
		latch.countUp();
	}

	public static void decrementWait() {
		latch.countDown();
	}

	public static class CountLatch {

		private static final class Sync extends AbstractQueuedSynchronizer {
			private static final long serialVersionUID = 1L;

			private Sync() {
			}

			@Override
			protected int tryAcquireShared(int acquires) {
				return getState() == 0 ? 1 : -1;
			}

			@Override
			protected boolean tryReleaseShared(int delta) {
				for (;;) {
					final int c = getState();
					final int nextc = c + delta;
					if (nextc < 0) {
						return false;
					}
					if (compareAndSetState(c, nextc)) {
						return nextc == 0;
					}
				}
			}
		}

		private final Sync sync = new Sync();

		public void countUp() {
			sync.releaseShared(1);
		}

		public void countDown() {
			sync.releaseShared(-1);
		}

		public void await() throws InterruptedException {
			sync.acquireSharedInterruptibly(1);
		}

	}

}
