package org.platformlayer.ops.pool;

public interface PoolBuilder<T> {
	String toKey(T item);

	Iterable<String> getItems();

	T toItem(String key);
}
