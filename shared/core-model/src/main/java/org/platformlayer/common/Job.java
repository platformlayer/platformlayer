package org.platformlayer.common;

import java.util.Date;

import org.platformlayer.core.model.Action;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.jobs.model.JobLog;

public interface Job {

	String getJobId();

	PlatformLayerKey getJobKey();

	PlatformLayerKey getTargetKey();

	JobState getState();

	Action getAction();

	Date getStartedAt();

	Date getEndedAt();

	JobLog getLog();
}
