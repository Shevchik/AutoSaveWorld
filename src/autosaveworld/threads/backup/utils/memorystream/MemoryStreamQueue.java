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

package autosaveworld.threads.backup.utils.memorystream;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MemoryStreamQueue {

	private static class Node {
		int item;

		Node(int b) {
			item = b;
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
		takeLock.lock();
		try {
			notEmpty.signal();
		} finally {
			takeLock.unlock();
		}
	}

	private void signalNotFull() {
		putLock.lock();
		try {
			notFull.signal();
		} finally {
			putLock.unlock();
		}
	}

	public MemoryStreamQueue() {
		this(Integer.MAX_VALUE);
	}

	public MemoryStreamQueue(int capacity) {
		if (capacity <= 0) {
			throw new IllegalArgumentException();
		}
		this.capacity = capacity;
		last = head = new Node(0);
	}

	public void put(int b) {
		put0(b & 0xFF);
	}

	private void put0(int b) {
		int c = -1;
		putLock.lock();
		while (count.get() >= capacity) {
			notFull.awaitUninterruptibly();
		}
		Node node = new Node(b);
		last = last.next = node;
		c = count.incrementAndGet();
		if (c < capacity) {
			notFull.signal();
		}
		putLock.unlock();
		if (c > 0) {
			signalNotEmpty();
		}
	}

	public void put(byte[] b, int off, int len) {
		int c = -1;
		putLock.lock();
		while (count.get() >= capacity) {
			notFull.awaitUninterruptibly();
		}
		for (int i = 0; i < len; i++) {
			Node node = new Node(b[off + i] & 0xFF);
			last = last.next = node;
		}
		c = count.addAndGet(len);
		if (c < capacity) {
			notFull.signal();
		}
		putLock.unlock();
		if (c > 0) {
			signalNotEmpty();
		}
	}

	private boolean eof = false;

	public void putEndOfStreamSignal() {
		put0(-1);
	}

	public int take() {
		if (eof) {
			return -1;
		}
		int c = -1;
		takeLock.lock();
		while (count.get() == 0) {
			notEmpty.awaitUninterruptibly();
		}
		int x = head.next.item;
		if (x == -1) {
			eof = true;
		}
		head = head.next;
		c = count.decrementAndGet();
		if (c > 0) {
			notEmpty.signal();
		}
		takeLock.unlock();
		if (c < capacity) {
			signalNotFull();
		}
		return x;
	}

	public int take(byte[] b, int off, int len) {
		if (eof) {
			return -1;
		}
		off--;
		int c = -1;
		takeLock.lock();
		while (count.get() == 0) {
			notEmpty.awaitUninterruptibly();
		}
		int n = Math.min(len, count.get());
		int i = 0;
		while (i < n) {
			int item = head.next.item;
			head = head.next;
			i++;
			if (item == -1) {
				eof = true;
				break;
			}
			b[off + i] = (byte) item;
		}
		c = count.addAndGet(-i);
		if (c > 0) {
			notEmpty.signal();
		}
		takeLock.unlock();
		if (c < capacity) {
			signalNotFull();
		}
		if (eof && (i == 1)) {
			return -1;
		}
		return i;
	}

}
