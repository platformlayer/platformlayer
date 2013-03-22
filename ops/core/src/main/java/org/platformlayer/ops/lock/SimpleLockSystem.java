package org.platformlayer.ops.lock;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;

import javax.inject.Singleton;

import com.google.common.collect.MapMaker;

@Singleton
public class SimpleLockSystem implements LockSystem {

	final ConcurrentMap<String, Lock> locks = new MapMaker().makeMap();

	@Override
	public Lock getLock(String lockKey) {
		Lock lock = locks.get(lockKey);
		if (lock == null) {
			lock = new SimpleLock();
			Lock existing = locks.put(lockKey, lock);
			if (existing != null) {
				lock = existing;
			}
		}
		return lock;
	}
}
