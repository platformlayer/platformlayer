package org.platformlayer.ops.schedule;

import java.util.Date;

public class JobExecution {
	public final Date startTimestamp;
	public final Date endTimestamp;
	public final Boolean success;
	public final Throwable error;

	public JobExecution(Date startTimestamp, Date endTimestamp, Boolean success, Throwable error) {
		this.startTimestamp = startTimestamp;
		this.endTimestamp = endTimestamp;
		this.success = success;
		this.error = error;
	}

	public Date getStartTimestamp() {
		return startTimestamp;
	}

}