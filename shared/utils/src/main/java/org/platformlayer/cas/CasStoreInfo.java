package org.platformlayer.cas;

public class CasStoreInfo {

	final boolean staging;

	public CasStoreInfo(boolean staging) {
		super();
		this.staging = staging;
	}

	public boolean isStaging() {
		return staging;
	}

}
