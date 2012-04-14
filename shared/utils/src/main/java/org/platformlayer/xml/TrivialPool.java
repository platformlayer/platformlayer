package org.platformlayer.xml;

import java.util.Stack;

/**
 * A trivial object pool, that will keep maxIdle objects and throw away others. It does not perform time-based cleanup.
 */
class TrivialPool<S> {
	final Stack<S> stack = new Stack<S>();

	final int maxSize;

	public TrivialPool(int maxSize) {
		super();
		this.maxSize = maxSize;
	}

	public synchronized void returnToPool(S item) {
		if (stack.size() < maxSize) {
			stack.push(item);
		}
	}

	public synchronized S tryBorrow() {
		if (stack.isEmpty()) {
			return null;
		}
		return stack.pop();
	}
}
