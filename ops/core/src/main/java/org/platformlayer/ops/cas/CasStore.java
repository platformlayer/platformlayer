package org.platformlayer.ops.cas;

import org.platformlayer.crypto.Md5Hash;

public interface CasStore {
	CasObject findArtifact(Md5Hash hash) throws Exception;
}
