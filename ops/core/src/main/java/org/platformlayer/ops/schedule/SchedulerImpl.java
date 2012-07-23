package org.platformlayer.ops.schedule;

import java.util.Date;
import java.util.Map;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.platformlayer.TimeSpan;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class SchedulerImpl implements Scheduler {
	static final Logger log = Logger.getLogger(SchedulerImpl.class);

	private static final TimeSpan MIN_DELAY = new TimeSpan("1s");

	private static final TimeSpan FORCE_RECALCULATE_INTERVAL = new TimeSpan("5m");

	@Inject
	ThreadServices threadServices;

	final Map<String, SchedulerJobState> stateMap = Maps.newHashMap();

	static class SchedulerJobState {
		private final Date nextExecution;
		private final TimerTask timerTask;

		public SchedulerJobState(Date nextExecution, TimerTask timerTask) {
			this.nextExecution = nextExecution;
			this.timerTask = timerTask;
		}
	}

	class SchedulerTimerTask implements Runnable {
		final JobScheduleCalculator schedule;
		final Runnable runnable;

		public SchedulerTimerTask(JobScheduleCalculator schedule, Runnable runnable) {
			this.schedule = schedule;
			this.runnable = runnable;
		}

		@Override
		public void run() {
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
				try {
					afterJobExecution(this, startTimestamp, endTimestamp, jobSuccess, jobException);
				} catch (Throwable t) {
					log.error("Error rescheduling job", t);
				}
			}
		}

		public JobScheduleCalculator getSchedule() {
			return schedule;
		}

		@Override
		public String toString() {
			return "SchedulerTimerTask [task=" + runnable + " schedule=" + schedule + "]";
		}

	}

	public void afterJobExecution(final SchedulerTimerTask schedulerTimerTask, final Date startTimestamp,
			final Date endTimestamp, final boolean jobSuccess, final Throwable jobException) {
		JobScheduleCalculator schedule = schedulerTimerTask.getSchedule();
		JobExecution managedJobExecution = new JobExecution(startTimestamp, jobSuccess);

		Date nextExecution = null;
		try {
			nextExecution = schedule.calculateNext(managedJobExecution);
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
						afterJobExecution(schedulerTimerTask, startTimestamp, endTimestamp, jobSuccess, jobException);
					} catch (Exception e) {
						log.error("Error rescheduling timer task", e);
					}
				}
			};
			threadServices.scheduleOneOff("RecalculateNext", timerTask, FORCE_RECALCULATE_INTERVAL);
		} else {
			scheduleNextExecution(nextExecution, schedulerTimerTask);
		}
	}

	private SchedulerJobState scheduleNextExecution(Date nextExecution, final SchedulerTimerTask schedulerTimerTask) {
		TimeSpan delay = TimeSpan.timeUntil(nextExecution);
		delay = TimeSpan.max(delay, MIN_DELAY);

		// We can't reschedule tasks, so we have to create a new one every time
		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				try {
					schedulerTimerTask.run();
				} catch (Exception e) {
					log.error("Error in timer task", e);
				}
			}
		};

		log.debug("Scheduling task: " + schedulerTimerTask + " for " + nextExecution);

		threadServices.scheduleOneOff("SchedulerJob", timerTask, delay);

		return new SchedulerJobState(nextExecution, timerTask);
	}

	private SchedulerJobState scheduleJob(JobScheduleCalculator schedule, Runnable runnable,
			JobExecution previousExecution) {
		Date nextExecution = schedule.calculateNext(previousExecution);
		if (nextExecution == null) {
			return new SchedulerJobState(null, null);
		}

		SchedulerTimerTask schedulerTimerTask = new SchedulerTimerTask(schedule, runnable);
		return scheduleNextExecution(nextExecution, schedulerTimerTask);
	}

	@Override
	public void putJob(String key, JobScheduleCalculator schedule, Runnable runnable) {
		SchedulerJobState state = stateMap.get(key);
		JobExecution previousExecution;
		if (state == null) {
			previousExecution = null;
		} else {
			// TODO: Cancel existing / reschedule??
			throw new UnsupportedOperationException();
		}

		state = scheduleJob(schedule, runnable, previousExecution);
		stateMap.put(key, state);
	}
}