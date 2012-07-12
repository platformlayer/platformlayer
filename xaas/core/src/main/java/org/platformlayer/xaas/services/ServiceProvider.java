package org.platformlayer.xaas.services;

import java.security.PublicKey;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.ServiceInfo;
import org.platformlayer.ids.ItemType;
import org.platformlayer.ids.ServiceType;
import org.platformlayer.metrics.model.MetricValues;
import org.platformlayer.ops.OpsException;
import org.platformlayer.xaas.model.ServiceAuthorization;

public interface ServiceProvider {
	void beforeCreateItem(ItemBase item) throws OpsException;

	void beforeDeleteItem(ItemBase item) throws OpsException;

	ServiceInfo getServiceInfo(boolean amdin);

	Models getModels();

	ServiceType getServiceType();

	void validateAuthorization(ServiceAuthorization serviceAuthorization) throws OpsException;

	void initialize();

	MetricValues getMetricValues(ItemBase item, String serviceKey) throws OpsException;

	Class<?> getJavaClass(ItemType itemType);

	boolean isSystemObject(ItemType itemType);

	ModelClass<?> getModelClass(ItemType itemType);

	PublicKey getSshPublicKey() throws OpsException;

	// Use getController(item)
	@Deprecated
	Class<?> getControllerClass(Class<?> javaClass) throws OpsException;

	// Use getController(item)
	@Deprecated
	Object getController(Class<?> itemClass) throws OpsException;

	Object getController(Object item) throws OpsException;
}
