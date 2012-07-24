package org.platformlayer.gwt.client.api.platformlayer;

public enum JobState {
	RUNNING, SUCCESS, FAILED;

	public boolean isComplete() {
		return this != RUNNING;
	}
};
