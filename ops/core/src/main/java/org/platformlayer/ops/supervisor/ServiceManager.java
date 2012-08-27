package org.platformlayer.ops.supervisor;

import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.tree.OpsTreeBase;

public interface ServiceManager {
	public void addServiceInstall(PlatformLayerKey owner, OpsTreeBase container) throws OpsException;

	public void addServiceInstance(StandardService service) throws OpsException;
}
