package org.platformlayer.cas;

import java.io.Closeable;
import java.io.File;

import org.platformlayer.ops.OpsException;

import com.fathomdb.hash.Md5Hash;

public interface CasStoreObject extends Closeable {
	CasLocation getLocation() throws OpsException;

	Md5Hash getHash();

	void copyTo(CasTarget target, File remoteFilePath, CasStore stagingStore) throws OpsException;

	CasStore getStore();
}
