package org.platformlayer;

import java.util.concurrent.Callable;

public interface CheckedCallable<T, E extends Exception> extends Callable<T> {
	@Override
	T call() throws E;
}
