package org.platformlayer.jobs.model;

import com.fathomdb.EnumWithKey;

public enum JobState implements EnumWithKey {
	PRESTART("P"), RUNNING("R"), FAILED("F"), SUCCESS("S");

	final String key;

	private JobState(String key) {
		this.key = key;
	}

	@Override
	public String getKey() {
		return key;
	}
}
