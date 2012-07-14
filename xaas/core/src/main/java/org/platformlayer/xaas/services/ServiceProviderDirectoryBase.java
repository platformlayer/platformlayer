package org.platformlayer.xaas.services;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.ServiceInfo;
import org.platformlayer.ids.ItemType;
import org.platformlayer.ids.ServiceType;
import org.platformlayer.xml.XmlHelper;
import org.platformlayer.xml.XmlHelper.ElementInfo;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Injector;

public abstract class ServiceProviderDirectoryBase implements ServiceProviderDictionary {
	final Map<ServiceType, ServiceProvider> serviceProviders = Maps.newHashMap();
	final Map<String, ServiceProvider> serviceProvidersByNamespace = Maps.newHashMap();
	final List<ServiceInfo> allServices = Lists.newArrayList();
	final Map<Class<?>, ModelClass> javaClassToModelClass = Maps.newHashMap();

	final Injector injector;

	public ServiceProviderDirectoryBase(Injector injector) {
		this.injector = injector;
	}

	@Override
	public ServiceProvider getServiceProvider(ServiceType serviceType) {
		return serviceProviders.get(serviceType);
	}

	@Override
	public ServiceProvider getServiceProviderForNamespace(String namespace) {
		return serviceProvidersByNamespace.get(namespace);
	}

	@Override
	public List<ServiceInfo> getAllServices() {
		return Collections.unmodifiableList(allServices);
	}

	public void addService(ServiceProvider serviceProvider) {
		ServiceType serviceType = serviceProvider.getServiceType();
		if (serviceProviders.containsKey(serviceType)) {
			throw new IllegalArgumentException("Duplicate service key: " + serviceType);
		}
		serviceProviders.put(serviceType, serviceProvider);

		for (ModelClass<?> modelClass : serviceProvider.getModels().all()) {
			String xmlNamespace = modelClass.getPrimaryNamespace();
			if (xmlNamespace == null) {
				throw new IllegalArgumentException("No XML namespace on model class: " + modelClass);
			}
			ServiceProvider existing = serviceProvidersByNamespace.get(xmlNamespace);
			if (existing != null && existing != serviceProvider) {
				throw new IllegalArgumentException("Duplicate XML namespace: " + xmlNamespace);
			}
			serviceProvidersByNamespace.put(xmlNamespace, serviceProvider);

			javaClassToModelClass.put(modelClass.getJavaClass(), modelClass);
		}

		ServiceInfo serviceInfo = serviceProvider.getServiceInfo();
		allServices.add(serviceInfo);
	}

	@Override
	public <T extends ItemBase> ModelClass<T> getModelClass(Class<T> itemClass) {
		ModelClass<T> modelClass = javaClassToModelClass.get(itemClass);
		if (modelClass == null) {
			// Fallback to matching on XML info
			ElementInfo elementInfo = XmlHelper.getXmlElementInfo(itemClass);

			if (elementInfo != null) {
				ServiceProvider serviceProvider = serviceProvidersByNamespace.get(elementInfo.namespace);
				if (serviceProvider != null) {
					ItemType itemType = new ItemType(elementInfo.elementName);
					modelClass = (ModelClass<T>) serviceProvider.getModelClass(itemType);
				}
			}
		}
		return modelClass;
	}

	private void addService(String className) {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Class<?> clazz = Class.forName(className, true, classLoader);
			addService(clazz);
		} catch (Exception e) {
			throw new IllegalStateException("Error building service: " + className, e);
		}
	}

	protected void addService(Class<?> clazz) {
		Object newInstance = injector.getInstance(clazz);
		addService((ServiceProvider) newInstance);
	}

}
