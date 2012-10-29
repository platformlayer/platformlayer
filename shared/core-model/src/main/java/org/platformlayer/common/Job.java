package org.platformlayer.common;

import org.platformlayer.core.model.Action;
import org.platformlayer.core.model.PlatformLayerKey;

public interface Job {

	String getJobId();

	PlatformLayerKey getJobKey();

	PlatformLayerKey getTargetKey();

	JobState getState();

	Action getAction();
}
