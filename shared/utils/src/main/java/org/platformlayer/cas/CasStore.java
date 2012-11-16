package org.platformlayer.cas;

import org.platformlayer.ops.OpsException;

import com.fathomdb.hash.Md5Hash;

public interface CasStore {
	CasStoreObject findArtifact(Md5Hash hash) throws OpsException;

	Md5Hash findTag(String tag) throws OpsException;

	int estimateDistance(CasLocation target) throws OpsException;
}
