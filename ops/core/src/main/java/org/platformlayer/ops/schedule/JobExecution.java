package org.platformlayer.ops.schedule;

import java.util.Date;

public class JobExecution {
	public final Date startTimestamp;
	public final Boolean success;

	public JobExecution(Date startTimestamp, Boolean success) {
		this.startTimestamp = startTimestamp;
		this.success = success;
	}

	public Date getStartTimestamp() {
		return startTimestamp;
	}

}