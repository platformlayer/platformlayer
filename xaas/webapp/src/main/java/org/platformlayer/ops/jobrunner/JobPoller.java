package org.platformlayer.ops.jobrunner;

import org.platformlayer.ops.OpsException;

import com.google.inject.ImplementedBy;

@ImplementedBy(JobPollerImpl.class)
public interface JobPoller {
	void start() throws OpsException;
}
