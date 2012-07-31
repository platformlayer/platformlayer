package org.platformlayer.cas;

import org.openstack.crypto.ByteString;
import org.platformlayer.ops.OpsException;

public interface CasStore {
	CasStoreObject findArtifact(ByteString hash) throws OpsException;

	ByteString findTag(String tag) throws OpsException;

	int estimateDistance(CasLocation target) throws OpsException;
}
