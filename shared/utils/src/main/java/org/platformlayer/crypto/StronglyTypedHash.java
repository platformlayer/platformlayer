package org.platformlayer.crypto;

import java.util.Arrays;

public abstract class StronglyTypedHash {
	final byte[] hash;

	protected StronglyTypedHash(byte[] hash) {
		this.hash = hash;
	}

	@Override
	public String toString() {
		return toHex();
	}

	public String toHex() {
		return CryptoUtils.toHex(hash);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(hash);
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
		// We do a byte-by-byte comparison to prevent timing attacks
		StronglyTypedHash other = (StronglyTypedHash) obj;
		if (other.hash.length != this.hash.length) {
			return false;
		}

		boolean areEqual = true;
		for (int i = 0; i < hash.length; i++) {
			if (other.hash[i] != this.hash[i]) {
				areEqual = false;
			}
		}

		return areEqual;
	}

}
