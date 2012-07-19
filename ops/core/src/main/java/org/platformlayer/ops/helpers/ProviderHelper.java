package org.platformlayer.ops.helpers;

import java.util.List;

import javax.inject.Inject;

import org.platformlayer.CastUtils;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.ServiceInfo;
import org.platformlayer.ids.ServiceType;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.xaas.services.ModelClass;
import org.platformlayer.xaas.services.ServiceProvider;
import org.platformlayer.xaas.services.ServiceProviderDictionary;

import com.google.common.collect.Lists;

public class ProviderHelper {
	@Inject
	ServiceProviderDictionary serviceProviderDictionary;

	@Inject
	PlatformLayerHelpers platformLayer;

	public List<ModelClass<?>> findModelsProviding(Class<?> serviceClass) throws OpsException {
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
		return CastUtils.checkedCast(controller, interfaceClass);
	}
}
