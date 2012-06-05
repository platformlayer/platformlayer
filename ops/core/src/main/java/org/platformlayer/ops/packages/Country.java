package org.platformlayer.ops.packages;

public enum Country {
	DE, US;

	public String getTld() {
		return toString().toLowerCase();
	}
}
