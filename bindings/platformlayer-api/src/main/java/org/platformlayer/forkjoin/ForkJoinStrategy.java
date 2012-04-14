package org.platformlayer.forkjoin;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public abstract class ForkJoinStrategy {
	public abstract <T> Future<T> execute(Callable<T> callable);
}
