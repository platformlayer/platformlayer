package org.platformlayer.forkjoin;

import java.util.concurrent.ExecutionException;

import org.platformlayer.CheckedFunction;
import org.platformlayer.common.JobCollection;
import org.platformlayer.common.UntypedItemCollection;
import org.platformlayer.common.UntypedItemCollectionBase;
import org.platformlayer.jobs.model.JobDataList;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class ListConcatentation<T> extends ToIterable<Iterable<T>> {

	public static <K, T, E extends Exception> Iterable<T> joinLists(ForkJoinStrategy forkJoinPool, Iterable<K> keys,
			final CheckedFunction<K, Iterable<T>, E> map) throws ExecutionException {
		Iterable<Iterable<T>> join = ToIterable.join(forkJoinPool, keys, map);

		return Lists.newArrayList(Iterables.concat(join));
	}

	public static <K, T, E extends Exception> UntypedItemCollection joinListsUntypedItems(
			ForkJoinStrategy forkJoinPool, Iterable<K> keys, final CheckedFunction<K, UntypedItemCollection, E> map)
			throws ExecutionException {
		Iterable<UntypedItemCollection> join = ToIterable.join(forkJoinPool, keys, map);

		return UntypedItemCollectionBase.concat(join);
	}

	public static <K, T, E extends Exception> JobCollection joinListsJobs(ForkJoinStrategy forkJoinPool,
			Iterable<K> keys, final CheckedFunction<K, JobCollection, E> map) throws ExecutionException {
		Iterable<JobCollection> join = ToIterable.join(forkJoinPool, keys, map);

		return JobDataList.concat(join);
	}

}
