package org.openstack.keystone.resources.user;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import com.fathomdb.TimeSpan;

@Singleton
public class AsyncExecutor {
	final ExecutorService executors = Executors.newCachedThreadPool();
	final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
	
	public void schedule(TimeSpan delay, final Runnable runnable) {
		scheduler.schedule(new Runnable() {

			@Override
			public void run() {
				executors.submit(runnable);
			}
			
		}, delay.getTotalMilliseconds(), TimeUnit.MILLISECONDS);
	}

}
