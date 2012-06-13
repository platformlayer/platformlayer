package org.platformlayer.cas;

import org.openstack.crypto.ByteString;

public interface CasStore {
	CasStoreObject findArtifact(ByteString hash) throws Exception;
}
