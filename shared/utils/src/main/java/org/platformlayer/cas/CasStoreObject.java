package org.platformlayer.cas;

import java.io.Closeable;
import java.io.File;

import org.openstack.crypto.ByteString;

public interface CasStoreObject extends Closeable {
	CasLocation getLocation() throws Exception;

	ByteString getHash();

	void copyTo(CasTarget target, File remoteFilePath) throws Exception;
}
