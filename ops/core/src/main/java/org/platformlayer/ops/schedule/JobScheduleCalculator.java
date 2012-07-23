package org.platformlayer.ops.schedule;

import java.util.Date;

public interface JobScheduleCalculator {
	Date calculateNext(JobExecution previousExecution);
}