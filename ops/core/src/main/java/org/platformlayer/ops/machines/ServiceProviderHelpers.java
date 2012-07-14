package org.platformlayer.ops.machines;

import java.util.List;

import javax.inject.Inject;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.ServiceInfo;
import org.platformlayer.ids.ItemType;
import org.platformlayer.ids.ServiceType;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.xaas.services.ModelClass;
import org.platformlayer.xaas.services.ServiceProvider;
import org.platformlayer.xaas.services.ServiceProviderDictionary;
import org.platformlayer.xml.XmlHelper.ElementInfo;

import com.google.common.collect.Lists;

public class ServiceProviderHelpers {
	@Inject
	ServiceProviderDictionary serviceProviderDictionary;

	public <T extends ItemBase> List<ModelClass<? extends T>> getModelSubclasses(Class<T> parent) {
		List<ModelClass<? extends T>> modelClasses = Lists.newArrayList();

		for (ServiceInfo serviceInfo : serviceProviderDictionary.getAllServices()) {
			ServiceType serviceType = new ServiceType(serviceInfo.serviceType);
			ServiceProvider serviceProvider = serviceProviderDictionary.getServiceProvider(serviceType);

			for (ModelClass<?> modelClass : serviceProvider.getModels().all()) {
				Class<?> javaClass = modelClass.getJavaClass();
				if (parent.isAssignableFrom(javaClass)) {
					modelClasses.add((ModelClass<? extends T>) modelClass);
				}
			}
		}

		return modelClasses;
	}

	public ServiceType getServiceType(Class<? extends ItemBase> itemType) {
		ModelClass<?> modelClass = serviceProviderDictionary.getModelClass(itemType);
		return modelClass.getServiceType();
	}

	public ModelClass<?> getModelClass(String namespaceUri, String nodeName) {
		ServiceProvider serviceProvider = serviceProviderDictionary.getServiceProviderForNamespace(namespaceUri);

		if (serviceProvider == null) {
			throw new IllegalArgumentException("Unknown namespace: " + namespaceUri);
		}

		ModelClass<?> modelClass = null;
		for (ModelClass<?> modelClassCandidate : serviceProvider.getModels().all()) {
			if (nodeName.equals(modelClassCandidate.getXmlElementName())) {
				modelClass = modelClassCandidate;
				break;
			}
		}

		if (modelClass == null) {
			throw new IllegalArgumentException("Unknown element name: " + nodeName);
		}

		return modelClass;
	}

	public ModelClass<?> getModelClass(PlatformLayerKey platformLayerKey) {
		OpsContext ops = OpsContext.get();
		ServiceProviderDictionary serviceProviderDictionary = ops.getInjector().getInstance(
				ServiceProviderDictionary.class);

		ServiceType serviceType = platformLayerKey.getServiceType();
		ItemType itemType = platformLayerKey.getItemType();

		ServiceProvider serviceProvider = serviceProviderDictionary.getServiceProvider(serviceType);
		if (serviceProvider == null) {
			throw new IllegalArgumentException();
		}
		ModelClass<?> modelClass = serviceProvider.getModelClass(itemType);
		if (modelClass == null) {
			throw new IllegalArgumentException();
		}
		return modelClass;
	}

	public ModelClass<?> getModelClass(ElementInfo xmlElementInfo) {
		return getModelClass(xmlElementInfo.namespace, xmlElementInfo.elementName);
	}

	public ItemBase toModelType(ModelClass<?> modelClass, ItemBase item) {
		Class<?> targetClass = modelClass.getJavaClass();
		if (targetClass.equals(item.getClass())) {
			return item;
		}

		// TODO: Serialize to XML, then deserialize to desired type
		throw new UnsupportedOperationException();
	}

}
