package org.platformlayer.ops;

import java.lang.reflect.Field;
import java.security.PublicKey;
import java.util.List;

import javax.inject.Inject;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.ServiceInfo;
import org.platformlayer.ids.ItemType;
import org.platformlayer.ids.ServiceType;
import org.platformlayer.inject.ObjectInjector;
import org.platformlayer.metrics.model.MetricValues;
import org.platformlayer.ops.auth.OpsAuthentication;
import org.platformlayer.ops.helpers.ServiceContext;
import org.platformlayer.ops.helpers.SshKey;
import org.platformlayer.xaas.Controller;
import org.platformlayer.xaas.Service;
import org.platformlayer.xaas.discovery.AnnotatedClass;
import org.platformlayer.xaas.discovery.AnnotationDiscovery;
import org.platformlayer.xaas.model.ServiceAuthorization;
import org.platformlayer.xaas.services.ModelClass;
import org.platformlayer.xaas.services.Models;
import org.platformlayer.xaas.services.ServiceProvider;

import com.google.common.collect.Lists;

public abstract class ServiceProviderBase implements ServiceProvider {
	private final ServiceType serviceType;

	private final String description;

	@Inject
	AnnotationDiscovery discovery;

	@Inject
	protected ObjectInjector injector;

	@Inject
	CloudContextRegistry cloudContextRegistry;

	public ServiceProviderBase() {
		Service serviceAnnotation = getServiceAnnotation();

		String key = serviceAnnotation.value();

		// this.serviceType = serviceType;
		this.description = key;
		this.serviceType = new ServiceType(key);
	}

	Models models;

	@Override
	public Models getModels() {
		if (models == null) {
			models = new Models(buildModels());
		}
		return models;
	}

	protected List<? extends ModelClass<?>> buildModels() {
		List<ModelClass<?>> modelClasses = Lists.newArrayList();
		for (AnnotatedClass clazz : discovery.findAnnotatedClasses(Controller.class)) {
			ModelClass<?> modelClass = asModelClass((Class<? extends ItemBase>) clazz.getSubjectClass());
			if (modelClass != null) {
				modelClasses.add(modelClass);
			}
		}
		return modelClasses;
	}

	private Service getServiceAnnotation() {
		return getClass().getAnnotation(Service.class);
	}

	@Override
	public ServiceType getServiceType() {
		return serviceType;
	}

	@Override
	public ServiceInfo getServiceInfo(boolean admin) {
		ServiceInfo serviceInfo = new ServiceInfo();
		serviceInfo.serviceType = getServiceType().getKey();
		serviceInfo.description = description;
		// serviceInfo.schema =

		for (ModelClass<?> modelClass : getModels().all()) {
			ItemType itemType = modelClass.getItemType();

			if (serviceInfo.getNamespace() == null) {
				serviceInfo.namespace = modelClass.getPrimaryNamespace();
			}

			if (modelClass.isSystemObject()) {
				if (!admin) {
					continue;
				}
				if (serviceInfo.adminTypes == null) {
					serviceInfo.adminTypes = Lists.newArrayList();
				}
				serviceInfo.adminTypes.add(itemType.getKey());
			} else {
				if (serviceInfo.publicTypes == null) {
					serviceInfo.publicTypes = Lists.newArrayList();
				}
				serviceInfo.publicTypes.add(itemType.getKey());
			}
		}

		return serviceInfo;
	}

	@Override
	public void initialize() {
	}

	@Override
	public void beforeCreateItem(ItemBase item) throws OpsException {
		resolveKeys(item);

		CreationValidator validator = Injection.getInstance(CreationValidator.class);
		validator.validateCreateItem(item);
	}

	@Override
	public void beforeDeleteItem(ItemBase managedItem) throws OpsException {
	}

	private ModelClass<?> asModelClass(Class<? extends ItemBase> clazz) {
		Class<? extends ServiceProviderBase> myClass = getClass();
		Package myPackage = myClass.getPackage();

		Package clazzPackage = clazz.getPackage();

		// TODO: This is a bit hacky..
		String packagePrefix = myPackage.getName() + ".";
		if (!clazzPackage.getName().startsWith(packagePrefix)) {
			return null;
		}

		Controller modelAnnotation = clazz.getAnnotation(Controller.class);
		if (modelAnnotation == null) {
			return null;
		}

		return ModelClass.publicModel(this, clazz);
	}

	@Override
	public void validateAuthorization(ServiceAuthorization serviceAuthorization) throws OpsException {
		OpsConfig opsConfig = OpsConfig.build(serviceAuthorization);

		OpsAuthentication opsAuth = null;
		UserInfo userInfo = new UserInfo(opsAuth, opsConfig);

		CloudContext cloudContext = cloudContextRegistry.getCloudContext(userInfo);
		cloudContext.validate();
	}

	@Override
	public Object getController(Class<?> managedItemClass) throws OpsException {
		Controller controller = managedItemClass.getAnnotation(Controller.class);
		if (controller == null) {
			throw new IllegalArgumentException("No @Controller annotation found for " + managedItemClass.getName());
		}

		ensureInitialized();

		return injector.getInstance(controller.value());
	}

	boolean initialized;

	void ensureInitialized() throws OpsException {
		if (initialized) {
			return;
		}

		ServiceInitializer initializer = injector.getInstance(ServiceInitializer.class);
		initializer.initialize(this);

		initialized = true;
	}

	@Override
	public MetricValues getMetricValues(ItemBase item, String metricKey) throws OpsException {
		MetricFetcher metricFetcher = injector.getInstance(MetricFetcher.class);
		return metricFetcher.fetch(this, item, metricKey);
	}

	@Override
	public Class<?> getJavaClass(ItemType itemType) {
		ModelClass<?> modelClass = findModelClass(itemType);
		if (modelClass == null) {
			return null;
		}

		return modelClass.getJavaClass();
	}

	@Override
	public ModelClass<?> getModelClass(ItemType itemType) {
		ModelClass<?> modelClass = findModelClass(itemType);
		return modelClass;
	}

	private ModelClass<?> findModelClass(ItemType itemType) {
		for (ModelClass<?> modelClass : getModels().all()) {
			if (!itemType.equals(modelClass.getItemType())) {
				continue;
			}

			return modelClass;
		}
		return null;
	}

	@Override
	public boolean isSystemObject(ItemType itemType) {
		ModelClass<?> modelClass = findModelClass(itemType);
		if (modelClass == null) {
			throw new IllegalArgumentException("Not found");
		}

		return modelClass.isSystemObject();
	}

	@Override
	public PublicKey getSshPublicKey() throws OpsException {
		ServiceContext serviceContext = injector.getInstance(ServiceContext.class);

		SshKey sshKey = serviceContext.getSshKey();
		if (sshKey == null) {
			return null;
		}
		PublicKey publicKey = sshKey.getKeyPair().getPublic();
		return publicKey;
	}

	public void resolveKeys(Object item) throws OpsException {
		Class<? extends Object> itemClass = item.getClass();
		for (Field field : itemClass.getFields()) {
			Class<?> fieldType = field.getType();

			if (fieldType == PlatformLayerKey.class) {
				PlatformLayerKey key;
				try {
					key = (PlatformLayerKey) field.get(item);
				} catch (IllegalAccessException e) {
					throw new IllegalStateException("Error getting field: " + field, e);
				}

				if (key != null) {
					if (key.getServiceType() == null) {
						ItemType itemType = key.getItemType();
						ServiceType serviceType = OpsContext.get().getOpsSystem().getServiceType(itemType);
						key = key.withServiceType(serviceType);
					}

					try {
						field.set(item, key);
					} catch (IllegalAccessException e) {
						throw new IllegalStateException("Error setting field: " + field, e);
					}
				}
			}
		}
	}
}
