package org.platformlayer.ops.supervisor;

import org.platformlayer.ops.OpsException;

public interface ServiceManager {
	public Class<?> getServiceManagerInstallClass();

	public void addServiceInstance(StandardService service) throws OpsException;
}
