package org.platformlayer.cas;

import org.openstack.crypto.ByteString;

public abstract class CasStoreObjectBase implements CasStoreObject {
	private final ByteString hash;

	public CasStoreObjectBase(ByteString hash) {
		this.hash = hash;
	}

	@Override
	public ByteString getHash() {
		return hash;
	}

	@Override
	public void close() {
	}

}
