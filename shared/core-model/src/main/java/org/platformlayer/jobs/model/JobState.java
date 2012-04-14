package org.platformlayer.jobs.model;

public enum JobState {
	RUNNING, FAILED, SUCCESS;

	public int getCode() {
		// TODO: Use stable value
		return ordinal();
	}
}
