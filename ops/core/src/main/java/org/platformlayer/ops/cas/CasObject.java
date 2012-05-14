package org.platformlayer.ops.cas;

import java.io.File;

import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;

public interface CasObject {
	void copyTo(OpsTarget target, File remoteFilePath) throws OpsException;
}
