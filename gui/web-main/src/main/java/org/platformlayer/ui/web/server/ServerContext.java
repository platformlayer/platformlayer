package org.platformlayer.ui.web.server;

import java.util.List;

import javax.inject.Inject;

import org.platformlayer.core.model.ServiceInfo;
import org.platformlayer.ids.ServiceType;
import org.platformlayer.xaas.services.ServiceProviderDictionary;

import com.google.inject.Injector;

public class ServerContext {
    static ThreadLocal<ServerContext> THREAD_CONTEXT = new ThreadLocal<ServerContext>();

    public static ServerContext get() {
        return THREAD_CONTEXT.get();
    }

    public static void set(ServerContext serverContext) {
        THREAD_CONTEXT.set(serverContext);
    }

    @Inject
    Injector injector;

    @Inject
    ServiceProviderDictionary serviceDictionary;

    ServiceInfo findServiceInfo(ServiceType serviceType) {
        boolean management = false;

        List<ServiceInfo> allServices = serviceDictionary.getAllServices(management);
        for (ServiceInfo service : allServices) {
            if (service.getServiceType().equals(serviceType.getKey())) {
                return service;
            }
        }

        return null;
    }

    public <T> T getInstance(Class<T> clazz) {
        return injector.getInstance(clazz);
    }
}