package org.platformlayer.cas;

import org.openstack.crypto.ByteString;

public interface CasStore {
	CasStoreObject findArtifact(ByteString hash) throws Exception;

	ByteString findTag(String tag) throws Exception;
}
