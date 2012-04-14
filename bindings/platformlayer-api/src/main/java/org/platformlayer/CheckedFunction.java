package org.platformlayer;

public interface CheckedFunction<K, V, E extends Exception> {
	public V apply(K input) throws E;
}
