package org.platformlayer.cas;

import com.fathomdb.hash.Md5Hash;

public abstract class CasStoreObjectBase implements CasStoreObject {
	private final CasStore store;
	private final Md5Hash hash;

	public CasStoreObjectBase(CasStore store, Md5Hash hash) {
		this.store = store;
		this.hash = hash;
	}

	@Override
	public Md5Hash getHash() {
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
