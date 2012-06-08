package org.platformlayer.ops.cas;

import java.io.File;

import org.platformlayer.crypto.Md5Hash;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.networks.NetworkPoint;

public interface CasObject {
	NetworkPoint getLocation() throws OpsException;

	Md5Hash getHash();

	void copyTo(OpsTarget target, File remoteFilePath) throws OpsException;
}
