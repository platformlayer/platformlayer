//package org.platformlayer.ops.machines;
//
//import java.util.List;
//
//import javax.inject.Inject;
//
//import org.platformlayer.core.model.ServiceInfo;
//import org.platformlayer.ids.ServiceType;
//import org.platformlayer.xaas.services.ServiceProvider;
//import org.platformlayer.xaas.services.ServiceProviderDictionary;
//
//import com.google.common.collect.Lists;
//
//public class CloudServiceProviderDictionary {
//    @Inject
//    ServiceProviderDictionary dictionary;
//
//    public static class SpecializedService {
//        private final ServiceProvider serviceProvider;
//
//        public SpecializedService(ServiceProvider serviceProvider) {
//            this.serviceProvider = serviceProvider;
//        }
//    }
//
//    List<SpecializedService> serviceMap;
//
//    List<SpecializedService> getServiceMap() {
//        if (serviceMap == null) {
//            List<SpecializedService> services = Lists.newArrayList();
//
//            boolean management = false;
//            for (ServiceInfo service : dictionary.getAllServices(management)) {
//                ServiceType serviceType = new ServiceType(service.getServiceType());
//                ServiceProvider serviceProvider = dictionary.getServiceProvider(serviceType);
//                Class<? extends ServiceProvider> serviceProviderClass = serviceProvider.getClass();
//                if (CloudController.class.isAssignableFrom(serviceProviderClass)) {
//                    services.add(new SpecializedService(serviceProvider));
//                }
//            }
//
//            serviceMap = services;
//        }
//        return serviceMap;
//    }
// }
