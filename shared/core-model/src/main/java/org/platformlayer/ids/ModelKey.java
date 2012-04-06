package org.platformlayer.ids;

@Deprecated
// Consider using PlatformLayerKey instead?
public class ModelKey {
    public ServiceType serviceType;
    public ItemType itemType;
    public ProjectId projectId;
    public ManagedItemId itemKey;

    public ModelKey(ServiceType serviceType, ItemType itemType, ProjectId projectId, ManagedItemId itemKey) {
        super();
        this.serviceType = serviceType;
        this.itemType = itemType;
        this.projectId = projectId;
        this.itemKey = itemKey;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public ProjectId getProject() {
        return projectId;
    }

    public ManagedItemId getItemKey() {
        return itemKey;
    }
}
