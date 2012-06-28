package org.platformlayer.xaas.services;

import java.util.List;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.ServiceInfo;
import org.platformlayer.ids.ServiceType;

public interface ServiceProviderDictionary {
	ServiceProvider getServiceProvider(ServiceType serviceType);

	<T extends ItemBase> ModelClass<T> getModelClass(Class<T> itemClass);

	List<ServiceInfo> getAllServices(boolean management);

	ServiceProvider getServiceProviderForNamespace(String namespaceURI);
}
