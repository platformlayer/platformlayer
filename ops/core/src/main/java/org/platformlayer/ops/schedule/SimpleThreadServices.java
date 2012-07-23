package org.platformlayer.ops.schedule;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.platformlayer.TimeSpan;

import com.google.inject.Singleton;

@Singleton
public class SimpleThreadServices implements ThreadServices {
	static final Logger log = Logger.getLogger(SimpleThreadServices.class);

	static final TimeSpan TIMER_PURGE_INTERVAL = TimeSpan.FIVE_MINUTES;

	final Timer sharedTimer;
	final ExecutorService executorService;

	public SimpleThreadServices() {
		this(true, Executors.newCachedThreadPool());
	}

	public SimpleThreadServices(boolean timerIsDaemon, ExecutorService executorService) {
		this.executorService = executorService;
		this.sharedTimer = new Timer("ThreadServices::sharedTimer", timerIsDaemon);

		if (TIMER_PURGE_INTERVAL != null) {
			sharedTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					try {
						sharedTimer.purge();
					} catch (Exception e) {
						log.error("Error while purging timer", e);
					}
				}
			}, TIMER_PURGE_INTERVAL.getTotalMilliseconds(), TIMER_PURGE_INTERVAL.getTotalMilliseconds());
		}
	}

	TimerTask wrap(final Runnable command) {
		return new TimerTask() {
			@Override
			public void run() {
				executorService.execute(command);
			}
		};
	}

	@Override
	public void schedule(String description, final TimerTask task, TimeSpan delay, TimeSpan interval) {
		TimerTask scheduleTask = wrap(task);

		sharedTimer.schedule(scheduleTask, delay.getTotalMilliseconds(), interval.getTotalMilliseconds());
	}

	@Override
	public void close() {
		log.warn("Shutting down thread pool");

		sharedTimer.cancel();
		executorService.shutdown();
	}

	@Override
	public void scheduleOneOff(String description, TimerTask task, TimeSpan delay) {
		TimerTask scheduleTask = wrap(task);

		sharedTimer.schedule(scheduleTask, delay.getTotalMilliseconds());
	}
}