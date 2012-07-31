package org.platformlayer.cas;

import org.openstack.crypto.ByteString;

public abstract class CasStoreObjectBase implements CasStoreObject {
	private final CasStore store;
	private final ByteString hash;

	public CasStoreObjectBase(CasStore store, ByteString hash) {
		this.store = store;
		this.hash = hash;
	}

	@Override
	public ByteString getHash() {
		return hash;
	}

	@Override
	public void close() {
	}

	@Override
	public CasStore getStore() {
		return store;
	}

}
