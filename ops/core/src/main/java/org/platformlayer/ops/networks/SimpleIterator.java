package org.platformlayer.ops.networks;

import java.util.Iterator;

public abstract class SimpleIterator<T> implements Iterator<T> {
	T next;

	protected SimpleIterator() {
		this.next = getNext(null);
	}

	@Override
	public boolean hasNext() {
		return next != null;
	}

	@Override
	public T next() {
		T ret = next;
		next = getNext(ret);
		return ret;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	protected abstract T getNext(T current);
}
