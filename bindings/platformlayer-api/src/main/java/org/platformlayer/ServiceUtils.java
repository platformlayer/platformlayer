package org.platformlayer;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.platformlayer.core.model.ServiceInfo;

import com.google.common.base.Joiner;

public class ServiceUtils {
	static final Logger log = Logger.getLogger(ServiceUtils.class);

	public static ServiceInfo findByNamespace(Collection<ServiceInfo> services, String namespace) {
		if (services != null) {
			for (ServiceInfo service : services) {
				if (namespace.equals(service.getNamespace())) {
					return service;
				}
			}
		}

		log.warn("Unable to find service: " + namespace + " in "
				+ (services != null ? Joiner.on(",").join(services) : null));

		return null;
	}
}
