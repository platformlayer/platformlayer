package org.platformlayer.xaas.services;

import java.security.PublicKey;
import java.util.List;

import org.platformlayer.core.model.Action;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.ServiceInfo;
import org.platformlayer.ids.ItemType;
import org.platformlayer.ids.ServiceType;
import org.platformlayer.metrics.model.MetricDataSource;
import org.platformlayer.metrics.model.MetricQuery;
import org.platformlayer.ops.OpsException;
import org.platformlayer.xaas.model.ServiceAuthorization;

public interface ServiceProvider {
	void beforeCreateItem(ItemBase item) throws OpsException;

	void beforeDeleteItem(ItemBase item) throws OpsException;

	ServiceInfo getServiceInfo();

	Models getModels();

	ServiceType getServiceType();

	void validateAuthorization(ServiceAuthorization serviceAuthorization) throws OpsException;

	void initialize();

	MetricDataSource getMetricValues(ItemBase item, MetricQuery query) throws OpsException;

	Class<?> getJavaClass(ItemType itemType);

	ModelClass<?> getModelClass(ItemType itemType);

	PublicKey getSshPublicKey() throws OpsException;

	// Use getController(item)
	@Deprecated
	Class<?> getControllerClass(Class<?> javaClass) throws OpsException;

	// Use getController(item)
	@Deprecated
	Object getController(Class<?> itemClass) throws OpsException;

	Object getController(Object item) throws OpsException;

	List<Class<? extends Action>> getActions();

	Object getExtensionResource();

	Object getItemExtensionResource(Object item) throws OpsException;
}
