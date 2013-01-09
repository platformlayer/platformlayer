package org.platformlayer.ops.schedule;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import org.slf4j.*;
import com.fathomdb.utils.Hex;
import org.platformlayer.HttpPlatformLayerClient;
import org.platformlayer.PlatformLayerClient;
import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.PlatformLayerEndpointInfo;
import org.platformlayer.RepositoryException;
import org.platformlayer.TimeSpan;
import org.platformlayer.auth.Authenticator;
import org.platformlayer.auth.DirectAuthenticationToken;
import org.platformlayer.auth.DirectAuthenticator;
import org.platformlayer.core.model.JobSchedule;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.http.HttpStrategy;
import org.platformlayer.ids.ProjectId;
import org.platformlayer.ops.OpsException;

import com.fathomdb.crypto.CryptoKey;
import com.fathomdb.crypto.FathomdbCrypto;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class SchedulerImpl implements Scheduler {
	static final Logger log = LoggerFactory.getLogger(SchedulerImpl.class);

	private static final TimeSpan MIN_DELAY = new TimeSpan("1s");

	private static final TimeSpan FORCE_RECALCULATE_INTERVAL = new TimeSpan("5m");

	@Inject
	ThreadServices threadServices;

	@Inject
	SchedulerRepository repository;

	@Inject
	HttpStrategy httpStrategy;

	final Map<String, SchedulerTask> tasks = Maps.newHashMap();

	boolean ready;

	@Override
	public void start() throws OpsException {
		ensureStarted();
	}

	class SchedulerTask {
		final String key;
		final JobScheduleCalculator schedule;
		final Runnable runnable;

		TimerTask scheduledTimerTask;
		Date scheduledExecution;

		volatile boolean cancelled = false;

		JobExecution previousExecution;

		public SchedulerTask(String key, JobScheduleCalculator schedule, Runnable runnable) {
			this.key = key;
			this.schedule = schedule;
			this.runnable = runnable;
		}

		private void runTask() {
			boolean jobSuccess = false;
			Throwable jobException = null;

			Date startTimestamp = new Date();
			Date endTimestamp;

			try {
				runnable.run();
				jobSuccess = true;
			} catch (Throwable t) {
				jobException = t;
			} finally {
				endTimestamp = new Date();
				JobExecution execution = new JobExecution(startTimestamp, endTimestamp, jobSuccess, jobException);
				previousExecution = execution;

				if (!cancelled) {
					try {
						scheduleTask(this, execution);
					} catch (Throwable t) {
						log.error("Error rescheduling job", t);
					}
				} else {
					log.debug("Task cancelled; won't reschedule");
				}

				try {
					repository.logExecution(key, execution, jobException);
				} catch (Throwable t) {
					log.error("Error logging job execution", t);
				}

			}
		}

		public synchronized void scheduleNextExecution(Date nextExecution) {
			if (cancelled) {
				log.debug("Task cancelled; won't schedule");
				return;
			}

			TimeSpan delay = TimeSpan.timeUntil(nextExecution);
			delay = TimeSpan.max(delay, MIN_DELAY);

			// We can't reissue tasks, so we have to create a new TimerTask every time
			// (as we want more complicated scheduling)
			TimerTask timerTask = new TimerTask() {
				@Override
				public void run() {
					try {
						runTask();
					} catch (Exception e) {
						log.error("Error in timer task", e);
					}
				}
			};

			log.debug("Scheduling task: " + this + " for " + nextExecution);

			threadServices.scheduleOneOff("SchedulerJob", timerTask, delay);

			this.scheduledTimerTask = timerTask;
			this.scheduledExecution = nextExecution;
		}

		public JobScheduleCalculator getSchedule() {
			return schedule;
		}

		@Override
		public String toString() {
			return "SchedulerTimerTask [task=" + runnable + " schedule=" + schedule + "]";
		}

		public synchronized JobExecution getPreviousExecution() {
			return previousExecution;
		}

		public synchronized void cancel() {
			cancelled = true;

			if (scheduledTimerTask != null) {
				if (!scheduledTimerTask.cancel()) {
					log.debug("Unable to cancel scheduled task - likely already started");
				}

				scheduledTimerTask = null;
			}
		}
	}

	void scheduleTask(final SchedulerTask task, final JobExecution jobExecution) {
		JobScheduleCalculator schedule = task.getSchedule();

		Date nextExecution = null;
		try {
			nextExecution = schedule.calculateNext(jobExecution);
		} catch (Exception e) {
			log.warn("Error while calculating next execution", e);
		}

		if (nextExecution == null) {
			// Give it a chance to change its mind and reschedule itself,
			// by asking it again in a few minutes

			// Note: this means that tasks cannot be cancelled at the moment
			// (we'll at least keep polling the calendar)
			TimerTask timerTask = new TimerTask() {
				@Override
				public void run() {
					try {
						scheduleTask(task, jobExecution);
					} catch (Exception e) {
						log.error("Error rescheduling timer task", e);
					}
				}
			};
			threadServices.scheduleOneOff("RecalculateNext", timerTask, FORCE_RECALCULATE_INTERVAL);
		} else {
			task.scheduleNextExecution(nextExecution);
		}
	}

	private Runnable toRunnable(SchedulerRecord record) {
		Task task = record.task;
		if (task instanceof ActionTask) {
			return toRunnable((ActionTask) task);
		} else {
			throw new UnsupportedOperationException();
		}
	}

	private static PlatformLayerEndpointInfo rehydrateEndpoint(final EndpointRecord in) {
		final Authenticator authenticator;
		final String platformlayerBaseUrl = in.url;
		final ProjectId projectId = new ProjectId(in.project);
		final List<String> trustKeys;

		if (Strings.isNullOrEmpty(in.trustKeys)) {
			trustKeys = Collections.emptyList();
		} else {
			trustKeys = Lists.newArrayList(Splitter.on(",").split(in.trustKeys));
		}
		{
			String token = in.token;
			CryptoKey secret = FathomdbCrypto.deserializeKey(Hex.fromHex(in.secret.plaintext()));
			DirectAuthenticationToken authenticationToken = new DirectAuthenticationToken(token, secret);
			authenticator = new DirectAuthenticator(authenticationToken);
		}

		PlatformLayerEndpointInfo out = new PlatformLayerEndpointInfo(authenticator, platformlayerBaseUrl, projectId,
				trustKeys);
		return out;
	}

	private Runnable toRunnable(final ActionTask task) {
		final PlatformLayerKey target = task.target;

		final PlatformLayerEndpointInfo endpoint = rehydrateEndpoint(task.endpoint);

		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					PlatformLayerClient platformLayer = HttpPlatformLayerClient.build(httpStrategy,
							endpoint.getPlatformlayerBaseUrl(), endpoint.getAuthenticator(), endpoint.getProjectId(),
							endpoint.getTrustKeys());

					platformLayer.doAction(target, task.action);

					// TODO: Wait for task completion??
					// TODO: Link job id??
				} catch (PlatformLayerClientException e) {
					log.warn("Error running action", e);
				}
			}

			@Override
			public String toString() {
				return task.action + " on " + task.target;
			}
		};

		return runnable;
	}

	private JobScheduleCalculator parseSchedule(JobSchedule schedule, boolean tolerant) {
		TimeSpan interval = null;
		Date base = null;

		if (schedule == null) {
			if (tolerant) {
				log.warn("Expected schedule; was null");
			} else {
				throw new IllegalArgumentException("Schedule is required");
			}
		} else {
			if (!Strings.isNullOrEmpty(schedule.interval)) {
				try {
					interval = TimeSpan.parse(schedule.interval);
				} catch (IllegalArgumentException e) {
					if (tolerant) {
						log.warn("Ignoring error parsing interval: " + schedule.interval, e);
					} else {
						throw new IllegalArgumentException("Invalid interval: " + schedule.interval, e);
					}
				}
			}

			if (schedule.base != null) {
				base = schedule.base;
			}
		}

		if (interval == null) {
			if (tolerant) {
				log.warn("Interval not provided; assuming default");
				interval = TimeSpan.ONE_HOUR;
			} else {
				throw new IllegalArgumentException("Interval is required");
			}
		}

		JobScheduleCalculator scheduleCalculator = new SimpleJobScheduleCalculator(interval, base);
		return scheduleCalculator;
	}

	synchronized void ensureStarted() throws OpsException {
		if (ready) {
			return;
		}

		try {
			for (SchedulerRecord record : repository.findAll()) {
				// TODO: Tolerate exceptions?
				scheduleRecord(record, true);
			}
		} catch (RepositoryException e) {
			throw new OpsException("Error initializing scheduled tasks", e);
		}

		ready = true;
	}

	@Override
	public void putJob(SchedulerRecord record) throws OpsException {
		ensureStarted();

		scheduleRecord(record, false);

		try {
			repository.put(record);
		} catch (RepositoryException e) {
			throw new OpsException("Error persisting record", e);
		}
	}

	private void scheduleRecord(SchedulerRecord record, boolean tolerant) {
		String key = record.key;

		JobScheduleCalculator schedule = parseSchedule(record.schedule, tolerant);

		// TODO: More lightweight synchronization?
		synchronized (tasks) {
			SchedulerTask task = tasks.get(key);
			JobExecution previousExecution = null;
			if (task != null) {
				previousExecution = task.getPreviousExecution();

				// The action may have changed, so we delete
				task.cancel();
				task = null;
			}

			Runnable runnable = toRunnable(record);

			task = new SchedulerTask(key, schedule, runnable);
			scheduleTask(task, previousExecution);

			tasks.put(key, task);
		}
	}
}