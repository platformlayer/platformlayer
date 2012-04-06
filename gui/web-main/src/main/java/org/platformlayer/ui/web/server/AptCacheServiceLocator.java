//package org.platformlayer.ui.web.server;
//
//import org.platformlayer.ids.ServiceType;
//import org.platformlayer.service.aptcache.v1.AptCacheService;
//
//import com.google.web.bindery.requestfactory.shared.Locator;
//
//public class AptCacheServiceLocator extends Locator<AptCacheService, String> {
//
//    @Override
//    public AptCacheService create(Class<? extends AptCacheService> clazz) {
//        if (!clazz.equals(AptCacheService.class))
//            throw new IllegalArgumentException();
//        return new AptCacheService();
//    }
//
//    @Override
//    public AptCacheService find(Class<? extends AptCacheService> clazz, String id) {
//        if (!clazz.equals(AptCacheService.class))
//            throw new IllegalArgumentException();
//        return ServerContext.get().findServiceInfo(new ServiceType(id));
//    }
//
//    @Override
//    public Class<AptCacheService> getDomainType() {
//        return AptCacheService.class;
//    }
//
//    @Override
//    public String getId(AptCacheService domainObject) {
//        return domainObject.getKey();
//    }
//
//    @Override
//    public Class<String> getIdType() {
//        return String.class;
//    }
//
//    @Override
//    public Object getVersion(AptCacheService domainObject) {
//        return 0;
//    }
//
// }
