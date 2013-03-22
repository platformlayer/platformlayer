package org.platformlayer.ops.lock;

import java.util.concurrent.locks.Lock;

import com.google.inject.ImplementedBy;

@ImplementedBy(SimpleLockSystem.class)
public interface LockSystem {
	Lock getLock(String lockKey);
}
