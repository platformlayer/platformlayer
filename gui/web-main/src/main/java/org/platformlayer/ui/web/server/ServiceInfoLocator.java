package org.platformlayer.ui.web.server;

import org.platformlayer.core.model.ServiceInfo;
import org.platformlayer.ids.ServiceType;

import com.google.web.bindery.requestfactory.shared.Locator;

public class ServiceInfoLocator extends Locator<ServiceInfo, String> {

    @Override
    public ServiceInfo create(Class<? extends ServiceInfo> clazz) {
        if (!clazz.equals(ServiceInfo.class))
            throw new IllegalArgumentException();
        return new ServiceInfo();
    }

    @Override
    public ServiceInfo find(Class<? extends ServiceInfo> clazz, String id) {
        if (!clazz.equals(ServiceInfo.class))
            throw new IllegalArgumentException();
        return ServerContext.get().findServiceInfo(new ServiceType(id));
    }

    @Override
    public Class<ServiceInfo> getDomainType() {
        return ServiceInfo.class;
    }

    @Override
    public String getId(ServiceInfo domainObject) {
        return domainObject.getServiceType();
    }

    @Override
    public Class<String> getIdType() {
        return String.class;
    }

    @Override
    public Object getVersion(ServiceInfo domainObject) {
        return 0;
    }

}
