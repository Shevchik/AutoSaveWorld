/**
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */

package autosaveworld.threads.backup.utils.memory;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class PrimitiveIntLinkedBlockingQueue {

	static class Node {
		int item;

		Node(int b) {
			this.item = b;
		}

		Node next;
	}

	private final int capacity;

	private final AtomicInteger count = new AtomicInteger(0);

	private Node head;
	private Node last;


	private final ReentrantLock takeLock = new ReentrantLock();
	private final Condition notEmpty = takeLock.newCondition();
	private final ReentrantLock putLock = new ReentrantLock();
	private final Condition notFull = putLock.newCondition();

	private void signalNotEmpty() {
		final ReentrantLock takeLock = this.takeLock;
		takeLock.lock();
		try {
			notEmpty.signal();
		} finally {
			takeLock.unlock();
		}
	}

	private void signalNotFull() {
		final ReentrantLock putLock = this.putLock;
		putLock.lock();
		try {
			notFull.signal();
		} finally {
			putLock.unlock();
		}
	}

	public PrimitiveIntLinkedBlockingQueue() {
		this(Integer.MAX_VALUE);
	}

	public PrimitiveIntLinkedBlockingQueue(int capacity) {
		if (capacity <= 0) {
			throw new IllegalArgumentException();
		}
		this.capacity = capacity;
		last = head = new Node(0);
	}

	public void put(int b) {
		int c = -1;
		Node node = new Node(b);
		final ReentrantLock putLock = this.putLock;
		final AtomicInteger count = this.count;
		putLock.lock();
		try {
			while (count.get() == capacity) {
				notFull.awaitUninterruptibly();
			}
			last = last.next = node;
			c = count.getAndIncrement();
			if (c + 1 < capacity) {
				notFull.signal();
			}
		} finally {
			putLock.unlock();
		}
		if (c == 0) {
			signalNotEmpty();
		}
	}

	public int take() {
		int x;
		int c = -1;
		final AtomicInteger count = this.count;
		final ReentrantLock takeLock = this.takeLock;
		takeLock.lock();
		try {
			while (count.get() == 0) {
				notEmpty.awaitUninterruptibly();
			}
			x = head.next.item;
			head = head.next;
			c = count.getAndDecrement();
			if (c > 1) {
				notEmpty.signal();
			}
		} finally {
			takeLock.unlock();
		}
		if (c == capacity) {
			signalNotFull();
		}
		return x;
	}

}
