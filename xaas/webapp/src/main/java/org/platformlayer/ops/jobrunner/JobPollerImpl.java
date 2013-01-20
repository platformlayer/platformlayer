package org.platformlayer.ops.jobrunner;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.platformlayer.TimeSpan;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.tasks.JobQueueEntry;
import org.platformlayer.ops.tasks.OperationQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class JobPollerImpl implements JobPoller {
	static final Logger log = LoggerFactory.getLogger(JobPollerImpl.class);

	@Inject
	OperationQueue queue;

	boolean ready;

	ExecutorService executor = Executors.newCachedThreadPool();

	private Thread thread;

	@Override
	public void start() throws OpsException {
		ensureStarted();
	}

	synchronized void ensureStarted() throws OpsException {
		if (ready) {
			return;
		}

		thread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						final JobQueueEntry entry = queue.take();

						if (entry == null) {
							TimeSpan.ONE_SECOND.doSafeSleep();
						} else {
							executor.execute(new Runnable() {

								@Override
								public void run() {
									try {
										queue.startJob(entry);
									} catch (Throwable t) {
										log.warn("Uncaught error while running jobs", t);
									}
								}

							});
						}
					} catch (Throwable t) {
						log.warn("Ignoring error while polling for jobs", t);
						TimeSpan.ONE_SECOND.doSafeSleep();
					}
				}
			}

		});
		thread.start();

		ready = true;
	}
}