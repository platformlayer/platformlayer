package org.platformlayer.ops.metrics.collectd;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.Injection;
import org.platformlayer.ops.OperationType;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;

public class ManagedService {
	String serviceKey;

	@Handler
	public void doOperation(OperationType operationType, OpsTarget target) throws OpsException {
		if (operationType.isConfigure()) {
			// TODO: Be much smarter here...
			target.executeCommand("service {0} restart", getServiceKey());
		}
	}

	public static ManagedService build(String serviceKey) {
		ManagedService service = Injection.getInstance(ManagedService.class);
		service.setServiceKey(serviceKey);
		return service;
	}

	public String getServiceKey() {
		return serviceKey;
	}

	public void setServiceKey(String serviceKey) {
		this.serviceKey = serviceKey;
	}

}
