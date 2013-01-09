package org.platformlayer.ops.schedule;

import org.slf4j.*;

public abstract class OpsGeneralTask {

	static final Logger log = LoggerFactory.getLogger(OpsGeneralTask.class);

	private boolean blockConcurrentRuns;
	private boolean waitForTreeReady;
	private final boolean waitForInitialized = true;

	// public void logFinishedNoError(boolean clearErrors) throws OpsException {
	// log.info("Task completed");
	// if (clearErrors) {
	// removeChildrenWhere(new InstanceOfPredicate<OpsNode>(OpsTaskAlert.class));
	// }
	// }
	//
	// public void logFinishedError(String key, AlertSeverity severity, String title, String message, Throwable e) {
	// if (e != null) {
	// log.error("Task completed with error", e);
	// } else {
	// log.error("Task completed with error: " + title);
	// }
	//
	// logError(key, severity, title, message, e);
	// }
	//
	// public void logError(String key, AlertSeverity severity, String title, String message, Throwable e) {
	// log.info("Raising alert. Title=" + title + " Message=" + message);
	//
	// ActiveJob job = JobThreadContext.getCurrentJob();
	// OpsTaskAlert alert = new OpsTaskAlert(key, severity, title, message, e, job, this);
	// try {
	// replaceChild(alert);
	// } catch (OpsException e1) {
	// throw new IllegalStateException("Error adding error node", e1);
	// }
	// }
	//
	// public void logError(String key, String message, Exception e) {
	// logError(key, AlertSeverity.Warning, key + " on " + getPath(), message, e);
	// }
	//
	// public Future<JobLog> runAsync(final boolean alwaysRunJob) {
	// Future<JobLog> futureOperation = getOpsSystem().getThreadServices().getExecutorService()
	// .submit(new Callable<JobLog>() {
	// @Override
	// public JobLog call() throws Exception {
	// return runNow(alwaysRunJob);
	// }
	// });
	// return futureOperation;
	// }
	//
	// public void setBlockConcurrentRuns(boolean blockConcurrentRuns) {
	// this.blockConcurrentRuns = blockConcurrentRuns;
	// }
	//
	// private OpsItem getTargetOpsItem() {
	// OpsItem parentItem = getAncestor(OpsItem.class);
	// if (parentItem == null) {
	// log.warn("No OpsItem ancestor for " + this + " path=" + this.getPath());
	// return null;
	// }
	// return parentItem;
	// }
	//
	// private static <K, V> boolean mapReferenceEquals(Map<K, V> left, Map<K, V> right) {
	// if (left.size() != right.size()) {
	// return false;
	// }
	// for (Map.Entry<K, V> leftEntry : left.entrySet()) {
	// V rightValue = right.get(leftEntry.getKey());
	// if (leftEntry.getValue() != rightValue) {
	// return false;
	// }
	// }
	// return true;
	// }
	//
	// private Map<String, OpsNode> buildChildMap(OpsNode node) {
	// Map<String, OpsNode> children = Maps.newHashMap();
	// for (OpsNode child : node.getChildren()) {
	// children.put(child.getKey(), child);
	// }
	// return children;
	// }
	//
	// protected abstract void run0(ContextBase taskContext) throws Exception;
	//
	// @Override
	// public String toString() {
	// return getClass().getSimpleName() + ": " + getPath();
	// }

	// /**
	// * Gets another running job from the children.
	// * If multiple, returns an arbitrary job.
	// * If none, returns null.
	// *
	// * @return
	// */
	// protected ActiveJob getConcurrentJob() {
	// for (ActiveJob child : getChildrenOfType(ActiveJob.class))
	// return child;
	// return null;
	// }

	// public ActiveJob getRunningJob(boolean ignoreSelf) {
	// ActiveJob currentJob = ignoreSelf ? JobThreadContext.getCurrentJob() : null;
	//
	// for (ActiveJob child : getChildrenOfType(ActiveJob.class)) {
	// if (ignoreSelf) {
	// if (child == currentJob) {
	// continue;
	// }
	// }
	// if (child.isRunning()) {
	// return child;
	// }
	// }
	// return null;
	// }
	//
	// protected ActiveJob getConcurrentJob() {
	// return getRunningJob(true);
	// }

	// public boolean isBlockConcurrentRuns() {
	// return blockConcurrentRuns;
	// }
	//
	// public boolean isWaitForTreeReady() {
	// return waitForTreeReady;
	// }
	//
	// public void setWaitForTreeReady(boolean waitForTreeReady) {
	// this.waitForTreeReady = waitForTreeReady;
	// }
	//
	// public boolean isWaitForInitialized() {
	// return waitForInitialized;
	// }
	//
	// public void setWaitForInitialized(boolean waitForInitialized) {
	// this.waitForInitialized = waitForInitialized;
	// }

	// public JobLog runNow(boolean alwaysStartJob) {
	// if (!isAttached()) {
	// log.info("Task not attached; exiting");
	// return null;
	// }
	//
	// if (getParent() == null) {
	// log.warn("Parent was null; suspicious " + this);
	// }
	//
	// if (!alwaysStartJob && !shouldStartJob()) {
	// log.debug("Not starting job: shouldStartJob returned false for " + this.getPath());
	// return null;
	// }
	//
	// JobLog jobLog = getOpsSystem().buildJobLog();
	// ActiveJob job = ActiveJob.beginJob(OpsGeneralTask.this, jobLog);
	// // Operation operation = job.getOperation();
	//
	// ContextBase taskContext = job.getContext();
	//
	// OpsServer server = smartGetServer(false);
	//
	// boolean success = true;
	//
	// try {
	// taskContext.begin();
	//
	// if (!shouldStartJob()) {
	// log.info("Won't start job");
	// } else {
	// success = false;
	//
	// Map<String, OpsNode> previousChildren = buildChildMap(this);
	//
	// taskContext.enterScope();
	//
	// try {
	// run0(taskContext);
	// success = true;
	// } finally {
	// taskContext.fireEndHooks(success);
	// taskContext.exitScope();
	// }
	//
	// afterExecution(success);
	//
	// Map<String, OpsNode> newChildren = buildChildMap(this);
	//
	// boolean clearErrors = mapReferenceEquals(previousChildren, newChildren);
	//
	// logFinishedNoError(clearErrors);
	// }
	// } catch (Throwable e) {
	// if (e instanceof InterruptedException) {
	// Thread.currentThread().interrupt();
	// }
	//
	// // Uncaught exceptions are treated as critical errors
	// logFinishedError("GeneralFailure", AlertSeverity.Critical, "Error running task " + e, "Unexpected error", e);
	// } finally {
	// try {
	// taskContext.end(success);
	// } catch (OpsException e) {
	// log.warn("Error shutting down task context", e);
	// }
	//
	// job.endJob();
	// job = null;
	// }
	//
	// if (server != null) {
	// server.reportTaskOutcome(this, success);
	// }
	//
	// return taskContext.getJobLog();
	// }
	//
	// protected void afterExecution(boolean success) {
	//
	// }
	//
	// protected boolean shouldStartJob() {
	// boolean shouldStartJob = false;
	//
	// OpsItem targetItem = getTargetOpsItem();
	// if (targetItem == null) {
	// log.warn("No target item");
	// return false;
	// }
	//
	// if (TaskSuspension.hasTaskSuspension(this)) {
	// log.info("Task is currently suspended");
	// } else if (isBlockConcurrentRuns() && getConcurrentJob() != null) {
	// log.info("Another instance of the job is already running");
	// } else if (isWaitForTreeReady() && !targetItem.isTreeReady()) {
	// log.info("The server is not yet ready");
	// checkUptime();
	// } else if (isWaitForInitialized() && !targetItem.isInitialized()) {
	// log.info("This item has not yet been initialized");
	// checkUptime();
	// } else {
	// shouldStartJob = true;
	// }
	//
	// return shouldStartJob;
	// }
	//
	// private void checkUptime() {
	// TimeSpan uptime = getOpsSystem().getUptime();
	// if (uptime.isGreaterThan(SUSPICIOUS_UPTIME_THRESHOLD)) {
	// throw new IllegalStateException("Server is not yet ready after suspicious threshold: " + uptime);
	// }
	// }
	//
	// public static JobSchedule buildSimpleScheduleFromInterval(String key, String defaultValue) {
	// TimeSpan interval = FathomConfig.getTimeSpan(key, defaultValue);
	// return buildScalableScheduleFromInterval(interval);
	// }
	//
	// public static JobSchedule buildSimpleScheduleFromInterval(TimeSpan interval) {
	// return new SimpleJobSchedule(interval);
	// }
	//
	// public static JobSchedule buildScalableScheduleFromInterval(String key, String defaultValue) {
	// TimeSpan interval = FathomConfig.getTimeSpan(key, defaultValue);
	// return buildScalableScheduleFromInterval(interval);
	// }
	//
	// public static JobSchedule buildScalableScheduleFromInterval(TimeSpan interval) {
	// TimeSpan minimumInterval = interval;
	// TimeSpan maximumInterval = interval.multiplyBy(4);
	// return new ScalableJobSchedule(null, minimumInterval, maximumInterval);
	// // return new SimpleJobSchedule(interval);
	// }
}