package org.platformlayer.ops.helpers;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.ServiceInfo;
import org.platformlayer.ids.ServiceType;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.xaas.services.ModelClass;
import org.platformlayer.xaas.services.ServiceProvider;
import org.platformlayer.xaas.services.ServiceProviderDictionary;

import com.fathomdb.Casts;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ProviderHelper {
	@Inject
	ServiceProviderDictionary serviceProviderDictionary;

	@Inject
	PlatformLayerHelpers platformLayer;

	private List<ModelClass<?>> findModelsProviding(Class<?> serviceClass) throws OpsException {
		List<ModelClass<?>> models = Lists.newArrayList();

		for (ServiceInfo service : serviceProviderDictionary.getAllServices()) {
			ServiceType serviceType = new ServiceType(service.getServiceType());
			ServiceProvider serviceProvider = serviceProviderDictionary.getServiceProvider(serviceType);
			for (ModelClass<?> model : serviceProvider.getModels().all()) {
				Class<?> controllerClass = serviceProvider.getControllerClass(model.getJavaClass());
				if (serviceClass.isAssignableFrom(controllerClass)) {
					models.add(model);
				}
			}
		}

		return models;
	}

	public static class ProviderOf<T> {
		final ModelClass<?> model;
		final ItemBase item;

		public ProviderOf(ModelClass<?> model, ItemBase item) {
			this.model = model;
			this.item = item;
		}

		public ItemBase getItem() {
			return item;
		}

		public T get() throws OpsException {
			Object controller = model.getProvider().getController(item);
			return (T) controller;
		}

	}

	public <T> List<ProviderOf<T>> listItemsProviding(Class<T> serviceClass) throws OpsException {
		List<ProviderOf<T>> providers = Lists.newArrayList();

		List<ModelClass<?>> models = findModelsProviding(serviceClass);
		for (ModelClass<? extends ItemBase> model : models) {
			for (ItemBase item : platformLayer.listItems(model.getJavaClass())) {
				providers.add(new ProviderOf<T>(model, item));
			}
		}

		return providers;
	}

	public <T> T toInterface(ItemBase item, Class<T> interfaceClass) throws OpsException {
		ModelClass<? extends ItemBase> modelClass = serviceProviderDictionary.getModelClass(item.getClass());

		Object controller = modelClass.getProvider().getController(item);
		return Casts.checkedCast(controller, interfaceClass);
	}

	public <T> List<ProviderOf<T>> listChildrenProviding(PlatformLayerKey parent, Class<T> serviceClass)
			throws OpsException {
		List<ProviderOf<T>> providers = Lists.newArrayList();

		Map<Class<?>, ModelClass<?>> modelClasses = Maps.newHashMap();
		for (ModelClass<?> model : findModelsProviding(serviceClass)) {
			modelClasses.put(model.getJavaClass(), model);
		}

		{
			Object parentItem = platformLayer.getItem(parent);
			ModelClass<?> modelClass = modelClasses.get(parentItem.getClass());

			if (modelClass != null) {
				providers.add(new ProviderOf<T>(modelClass, (ItemBase) parentItem));
			}
		}

		for (ItemBase item : platformLayer.listChildrenTyped(parent)) {
			// Object item = platformLayer.promoteToTyped(untypedItem);
			ModelClass<?> modelClass = modelClasses.get(item.getClass());

			if (modelClass != null) {
				providers.add(new ProviderOf<T>(modelClass, item));
			}
		}

		return providers;
	}

}
