package org.platformlayer.common;

public enum JobState {
	RUNNING, FAILED, SUCCESS;

	public int getCode() {
		// TODO: Use stable value
		return ordinal();
	}
}
