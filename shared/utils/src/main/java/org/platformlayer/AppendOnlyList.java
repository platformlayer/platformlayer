package org.platformlayer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

public class AppendOnlyList<T> implements Iterable<T>, Serializable {
	private static final long serialVersionUID = 1L;

	final ArrayList<T> list = new ArrayList<T>();

	public void add(T item) {
		synchronized (list) {
			list.add(item);
		}
	}

	@Override
	public Iterator<T> iterator() {
		return new AppendOnlyListIterator();
	}

	class AppendOnlyListIterator implements Iterator<T> {
		final int size;
		int position = -1;

		AppendOnlyListIterator() {
			size = size();
		}

		@Override
		public boolean hasNext() {
			if ((position + 1) < size) {
				return true;
			}
			return false;
		}

		@Override
		public T next() {
			synchronized (list) {
				position++;
				return list.get(position);
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	public int size() {
		synchronized (list) {
			return list.size();
		}
	}

	public static <D> AppendOnlyList<D> create() {
		return new AppendOnlyList<D>();
	}
}
