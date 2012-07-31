package org.platformlayer.cas;

import java.io.Closeable;
import java.io.File;

import org.openstack.crypto.ByteString;
import org.platformlayer.ops.OpsException;

public interface CasStoreObject extends Closeable {
	CasLocation getLocation() throws OpsException;

	ByteString getHash();

	void copyTo(CasTarget target, File remoteFilePath, CasStore stagingStore) throws OpsException;

	CasStore getStore();
}
