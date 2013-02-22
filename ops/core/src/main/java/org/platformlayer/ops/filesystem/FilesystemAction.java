package org.platformlayer.ops.filesystem;

import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;

public interface FilesystemAction {

	void execute(OpsTarget target, ManagedFilesystemItem managedFilesystemItem) throws OpsException;

}
