package org.platformlayer;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

/*
 * Helper class that (synchronously) executes a function at set intervals until it returns non-null, or until a timeout is reached.
 */
public class TimeoutPoll<T> {
	static final Logger log = Logger.getLogger(TimeoutPoll.class);

	public static interface PollFunction<T> {
		public T call() throws Exception;

		static final Object YES = new Object();
		static final Object NO = null;
	}

	public static <T> T poll(TimeSpan timeout, TimeSpan pollInterval, PollFunction<T> untilNotNull)
			throws ExecutionException, TimeoutException {
		long start = System.currentTimeMillis();
		T value = null;
		do {
			try {
				value = untilNotNull.call();
			} catch (Exception e) {
				throw new ExecutionException(e);
			}

			if (value != null) {
				return value;
			}

			if (!pollInterval.doSafeSleep()) {
				throw new TimeoutException("Interrupted during sleep");
			}
		} while (!timeout.hasTimedOut(start));

		throw new TimeoutException();
	}

}