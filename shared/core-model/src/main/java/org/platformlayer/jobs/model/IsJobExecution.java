package org.platformlayer.jobs.model;

import java.util.Date;

import org.platformlayer.core.model.PlatformLayerKey;

public interface IsJobExecution {
	PlatformLayerKey getJobKey();

	JobState getState();

	Date getStartedAt();

	Date getEndedAt();
}
