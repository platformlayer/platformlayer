package org.platformlayer.ops;

import java.util.List;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.ServiceInfo;
import org.platformlayer.core.model.Tag;
import org.platformlayer.ids.ItemType;
import org.platformlayer.ids.ModelKey;
import org.platformlayer.ids.ProjectId;
import org.platformlayer.ids.ServiceType;
import org.platformlayer.ops.auth.OpsAuthentication;
import org.platformlayer.ops.jobs.JobRegistry;
import org.platformlayer.ops.ssh.ISshContext;
import org.platformlayer.ops.tasks.OperationQueue;
import org.platformlayer.xaas.repository.ManagedItemRepository;
import org.platformlayer.xaas.services.ServiceProvider;
import org.platformlayer.xaas.services.ServiceProviderDictionary;

import com.google.common.collect.Lists;
import com.google.inject.Injector;

@Singleton
public class OpsSystem {
    @Inject
    ServiceProviderDictionary serviceDictionary;

    @Inject
    ExecutorService executorService;

    @Inject
    ISshContext sshContext;

    @Inject
    OpsConfiguration configuration;

    @Inject
    OperationQueue operationQueue;

    @Inject
    Injector injector;

    @Inject
    JobRegistry jobRegistry;

    @Inject
    ManagedItemRepository managedItemRepository;

    @Inject
    Provider<OpsAuthentication> authenticationProvider;

    private static OpsSystem INSTANCE;

    public OpsSystem() {
        if (INSTANCE != null) {
            throw new IllegalStateException();
        }
        INSTANCE = this;
    }

    public ISshContext getSshContext() {
        return sshContext;
    }

    public OpsConfiguration getConfiguration() {
        return configuration;
    }

    public OperationQueue getOperationQueue() {
        return operationQueue;
    }

    public Injector getInjector() {
        return injector;
    }

    public JobRegistry getJobRegistry() {
        return jobRegistry;
    }

    public ManagedItemRepository getManagedItemRepository() {
        return managedItemRepository;
    }

    public Class<?> getJavaClass(PlatformLayerKey key) {
        ServiceProvider serviceProvider = getServiceProvider(key.getServiceType());
        return serviceProvider.getJavaClass(key.getItemType());
    }

    public Class<?> getJavaClass(ModelKey key) {
        ServiceProvider serviceProvider = getServiceProvider(key.getServiceType());
        return serviceProvider.getJavaClass(key.getItemType());
    }

    public ServiceProvider getServiceProvider(ServiceType serviceType) {
        return serviceDictionary.getServiceProvider(serviceType);
    }

    public boolean isSystemObject(ModelKey modelKey) {
        ServiceProvider serviceProvider = getServiceProvider(modelKey.getServiceType());
        return serviceProvider.isSystemObject(modelKey.getItemType());
    }

    // public ModelKey buildModelKey(ItemBase item) {
    // ModelClass<? extends ItemBase> modelClass = serviceDictionary.getModelClass(item.getClass());
    //
    // if (modelClass == null) {
    // throw new IllegalStateException("Unknown item class " + item.getClass());
    // }
    //
    // ManagedItemId itemKey = new ManagedItemId(item.getId());
    // ModelKey modelKey = new ModelKey(modelClass.getServiceType(), modelClass.getItemType(), getProject(), itemKey);
    // return modelKey;
    // }

    private ProjectId getProject() {
        // TODO: Can we just do this?
        // return Scope.get().get(ProjectId.class)
        OpsAuthentication authentication = authenticationProvider.get();
        if (authentication == null) {
            throw new SecurityException("Not authorized");
        }
        return authentication.getProjectId();
    }

    // public Tag createPlatformLayerLink(ItemBase item) {
    // ModelKey modelKey = buildModelKey(item);
    // return new Tag(Tag.CONDUCTOR_ID, buildUrl(modelKey));
    // }

    // public Tag createExternalLink(ItemBase item) {
    // ModelKey modelKey = buildModelKey(item);
    // return new Tag(Tag.PLATFORM_LAYER_ID, buildUrl(modelKey));
    // }

    // public Tag createTag(String key, ItemBase item) {
    // ModelKey modelKey = buildModelKey(item);
    // return new Tag(key, buildUrl(modelKey));
    // }

    public Tag createParentTag(ItemBase parent) {
        PlatformLayerKey parentKey = OpsSystem.toKey(parent);
        return Tag.buildParentTag(parentKey);
    }

    public Tag createParentTag(ModelKey modelKey) {
        return new Tag(Tag.PARENT, buildUrl(modelKey));
    }

    public String buildUrl(ModelKey modelKey) {
        String url = modelKey.getProject().getKey() + "/" + modelKey.getServiceType().getKey() + "/" + modelKey.getItemType().getKey();
        if (modelKey.getItemKey() != null) {
            url += "/" + modelKey.getItemKey().getKey();
        }
        return url;
    }

    // public Tag createOwnerTag(ItemBase item) {
    // ModelKey modelKey = buildModelKey(item);
    // return new Tag(Tag.OWNER_ID, buildUrl(modelKey));
    // }

    // public static PlatformLayerKey toKey(Class<? extends ItemBase> itemClass, ManagedItemId id) {
    // ServiceProviderDictionary serviceProviderDictionary =
    // OpsSystem.get().getInjector().getInstance(ServiceProviderDictionary.class);
    //
    // ModelClass<?> modelClass = serviceProviderDictionary.getModelClass(itemClass);
    // if (modelClass == null)
    // throw new IllegalArgumentException();
    // return new PlatformLayerKey(modelClass.getServiceType(), modelClass.getItemType(), id);
    // }

    public static PlatformLayerKey toKey(ItemBase item) {
        if (item.key != null) {
            return item.key;
        }

        throw new IllegalStateException();
        // return toKey(item.getClass(), new ManagedItemId(item.getId()));
    }

    // public static PlatformLayerKey toKey(String path) throws OpsException {
    // if (path == null)
    // return null;
    //
    // ServiceType serviceType;
    // ItemType itemType;
    // ManagedItemId id;
    //
    // String[] components = path.split("/");
    // if (components.length == 3) {
    // serviceType = new ServiceType(components[0]);
    // itemType = new ItemType(components[1]);
    // id = new ManagedItemId(components[2]);
    // } else if (components.length == 2) {
    // // serviceType is omitted
    // itemType = new ItemType(components[0]);
    // id = new ManagedItemId(components[1]);
    //
    // OpsSystem opsSystem = OpsContext.get().getOpsSystem();
    // serviceType = opsSystem.getServiceType(itemType);
    // } else {
    // throw new OpsException("Cannot parse path: " + path);
    // }
    // return new PlatformLayerKey(serviceType, itemType, id);
    // }

    public ServiceType getServiceType(ItemType findItemType) throws OpsException {
        List<String> serviceTypes = Lists.newArrayList();

        for (ServiceInfo service : serviceDictionary.getAllServices(false)) {
            for (String itemType : service.publicTypes) {
                if (itemType.equals(findItemType.getKey())) {
                    serviceTypes.add(service.getServiceType());
                }
            }
        }
        if (serviceTypes.size() == 1) {
            return new ServiceType(serviceTypes.get(0));
        }

        if (serviceTypes.size() == 0) {
            throw new OpsException("Item type cannot be resolved: " + findItemType);
        }

        throw new OpsException("Item type is ambiguous: " + findItemType);
    }

    public static String getPlatformLayerUrlBase() {
        return "http://127.0.0.1:8082/v0/";
    }

    public static OpsSystem get() {
        if (INSTANCE == null)
            throw new IllegalStateException();
        return INSTANCE;
    }

    // private <T> PlatformLayerKey toKey(Class<T> itemClass) throws OpsException {
    // for (ServiceInfo service : serviceDictionary.getAllServices(false)) {
    // ServiceType serviceType = new ServiceType(service.getServiceType());
    // ServiceProvider serviceProvider = serviceDictionary.getServiceProvider(serviceType);
    // for (ModelClass<?> model : serviceProvider.getModels().all()) {
    // if (model.getJavaClass() == itemClass) {
    // return new PlatformLayerKey(null, null, serviceType, model.getItemType(), null);
    // }
    // }
    // }
    //
    // throw new OpsException("Cannot find provider: " + itemClass);
    // }
    //
    // public static <T> PlatformLayerKey toKey(Class<T> itemClass, String itemId) throws OpsException {
    // PlatformLayerKey plk = OpsSystem.get().toKey(itemClass);
    // return plk.withId(new ManagedItemId(itemId));
    // }
}
