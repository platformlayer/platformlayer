package org.platformlayer.forkjoin;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Useful for Java 6 and for easier debugging!!
 * 
 * @author justinsb
 * 
 */
public class FakeForkJoinStrategy extends ForkJoinStrategy {

	@Override
	public <T> Future<T> execute(final Callable<T> callable) {

		Future<T> future = new Future<T>() {
			private T value = null;
			private boolean done = false;
			private Exception exception;

			@Override
			public boolean cancel(boolean mayInterruptIfRunning) {
				return false;
			}

			@Override
			public boolean isCancelled() {
				return false;
			}

			@Override
			public boolean isDone() {
				return done;
			}

			@Override
			public T get() throws InterruptedException, ExecutionException {
				if (!done) {
					try {
						value = callable.call();
					} catch (Exception e) {
						this.exception = e;
					}
					done = true;
				}

				if (this.exception != null) {
					throw new ExecutionException(exception);
				}

				return value;
			}

			@Override
			public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
				throw new UnsupportedOperationException();
			}
		};

		return future;
	}
}
