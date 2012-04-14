package org.platformlayer.forkjoin;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.platformlayer.CheckedFunction;

import com.google.common.collect.Lists;

public class ToIterable<T> {
	final List<Future<T>> tasks = Lists.newArrayList();

	public void add(Future<T> task) {
		tasks.add(task);
	}

	public static <K, T, E extends Exception> Iterable<T> join(ForkJoinStrategy forkJoinPool, Iterable<K> keys,
			final CheckedFunction<K, T, E> map) throws ExecutionException {
		ToIterable<T> concat = new ToIterable<T>();

		for (final K key : keys) {
			Future<T> future = forkJoinPool.execute(new Callable<T>() {
				@Override
				public T call() throws Exception {
					return map.apply(key);
				}
			});
			concat.add(future);
		}

		return concat.getResults();
	}

	public Iterable<T> getResults() throws ExecutionException {
		List<T> results = Lists.newArrayList();

		for (Future<T> task : tasks) {
			T value;
			try {
				value = task.get();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new ExecutionException(e);
			}
			results.add(value);
		}

		return results;
	}
}
