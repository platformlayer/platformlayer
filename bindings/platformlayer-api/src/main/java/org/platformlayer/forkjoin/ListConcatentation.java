package org.platformlayer.forkjoin;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.platformlayer.CheckedFunction;
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

	public static <K, T, E extends Exception> List<T> joinListsTypedItems(ForkJoinStrategy forkJoinPool,
			Iterable<K> keys, final CheckedFunction<K, ? extends Iterable<T>, E> map) throws ExecutionException {
		Iterable<? extends Iterable<T>> join = ToIterable.join(forkJoinPool, keys, map);

		List<T> ret = Lists.newArrayList();
		for (Iterable<T> c : join) {
			for (T t : c) {
				ret.add(t);
			}
		}
		return ret;
	}

	public static <K, T, E extends Exception> JobDataList joinListsJobs(ForkJoinStrategy forkJoinPool,
			Iterable<K> keys, final CheckedFunction<K, JobDataList, E> map) throws ExecutionException {
		Iterable<JobDataList> join = ToIterable.join(forkJoinPool, keys, map);

		return JobDataList.concat(join);
	}

}
