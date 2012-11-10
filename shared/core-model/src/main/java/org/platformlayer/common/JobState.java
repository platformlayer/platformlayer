package org.platformlayer.common;

import org.platformlayer.shared.EnumWithKey;

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
