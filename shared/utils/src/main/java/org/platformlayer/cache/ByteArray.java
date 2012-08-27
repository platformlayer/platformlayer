package org.platformlayer.cache;

import java.util.Arrays;

public class ByteArray {
	private final byte[] array;

	public ByteArray(byte[] array) {
		this.array = array;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(array);
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
		ByteArray other = (ByteArray) obj;
		if (!Arrays.equals(array, other.array)) {
			return false;
		}
		return true;
	}

}
