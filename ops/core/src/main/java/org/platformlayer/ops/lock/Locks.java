package org.platformlayer.ops.lock;

import java.util.concurrent.locks.Lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Locks {
	private static final Logger log = LoggerFactory.getLogger(Locks.class);

	public static void unlock(Lock lock) {
		if (lock != null) {
			try {
				lock.unlock();
			} catch (Exception e) {
				log.warn("Error unlocking lock", e);
			}
		}
	}
}
