package org.platformlayer.cache;

import java.util.Arrays;

public class CacheKey {
	private final Object[] arguments;

	public CacheKey(Object[] arguments) {
		this.arguments = new Object[arguments.length];

		for (int i = 0; i < arguments.length; i++) {
			Object argument = arguments[i];
			if (argument != null) {
				Class<? extends Object> argumentClass = argument.getClass();
				if (argumentClass.isArray()) {
					if (argument instanceof byte[]) {
						argument = new ByteArray((byte[]) argument);
					} else {
						throw new IllegalArgumentException();
					}
				}
			}
			this.arguments[i] = argument;
		}
	}

	@Override
	public String toString() {
		return "CacheKey [arguments=" + Arrays.toString(arguments) + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(arguments);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		CacheKey other = (CacheKey) obj;
		if (!Arrays.equals(arguments, other.arguments)) {
			return false;
		}
		return true;
	}

}
