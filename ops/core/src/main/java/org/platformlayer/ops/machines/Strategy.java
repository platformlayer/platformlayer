package org.platformlayer.ops.machines;


public abstract class Strategy<T> {
	public abstract T choose(T a, T b);
}
