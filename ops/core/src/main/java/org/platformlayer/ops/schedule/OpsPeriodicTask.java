package org.platformlayer.ops.schedule;

import org.slf4j.*;
import org.platformlayer.core.model.Action;

public abstract class OpsPeriodicTask {
	static final Logger log = LoggerFactory.getLogger(OpsPeriodicTask.class);

	final JobScheduleCalculator jobSchedule;

	final Action action;

	public OpsPeriodicTask(JobScheduleCalculator jobSchedule, Action action) {
		super();
		this.jobSchedule = jobSchedule;
		this.action = action;
	}

	// private MyTimerTask timerTask;
	//
	// class MyTimerTask extends TimerTask {
	// @Override
	// public void run() {
	// runAsync(false);
	// }
	// }
	//
	// public OpsPeriodicTask(OpsSystem opsSystem, String key, JobSchedule jobSchedule) {
	// super(opsSystem, key);
	//
	// this.jobSchedule = jobSchedule != null ? jobSchedule : (JobSchedule) this;
	//
	// setBlockConcurrentRuns(true);
	// }
	//
	// @Override
	// protected void attached() throws OpsException {
	// timerTask = new MyTimerTask();
	//
	// JobExecution previousExecution = getLastRun();
	// getOpsSystem().getScheduler().scheduleJob(jobSchedule, timerTask, previousExecution);
	// }
	//
	// protected JobExecution getLastRun() {
	// // By default, we don't keep track of previous executions.
	// // ManagedJob based classes override this
	// return null;
	// }
	//
	// @Override
	// protected void detached() {
	// if (timerTask != null) {
	// timerTask.cancel();
	// }
	//
	// timerTask = null;
	// }
	//
	// @Action(caption = "Suspend for 1 hour")
	// public void suspendFor1Hour() throws OpsException {
	// suspendTask(new TimeSpan("1h"));
	// }
	//
	// @Action(caption = "Suspend for 1 day")
	// public void suspendFor1Day() throws OpsException {
	// suspendTask(new TimeSpan("1d"));
	// }
	//
	// @Action(caption = "Suspend for 1 week")
	// public void suspendFor1Week() throws OpsException {
	// suspendTask(new TimeSpan("1w"));
	// }
	//
	// private void suspendTask(TimeSpan timeSpan) throws OpsException {
	// Date expiry = timeSpan.addTo(new Date());
	// OpsNode subject = this;
	// if (subject == null) {
	// throw new IllegalStateException();
	// }
	// TaskSuspension suspension = new TaskSuspension(getOpsSystem(), "suspend-"
	// + NodeUtils.sanitizeKeyName(subject.getPath()), null);
	// suspension.setExpiresAt(expiry);
	// suspension.setSubject(subject.getPath());
	// getOpsSystem().addRootItem(suspension, true);
	// }

	public JobScheduleCalculator getJobSchedule() {
		return jobSchedule;
	}

	public Action getAction() {
		return action;
	}

}