package org.platformlayer;

import java.util.Collection;

import org.platformlayer.core.model.ServiceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

public class ServiceUtils {
	private static final Logger log = LoggerFactory.getLogger(ServiceUtils.class);

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
