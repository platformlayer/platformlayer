//package org.platformlayer.ui.web.server;
//
//import java.util.List;
//
//import javax.inject.Inject;
//import javax.inject.Provider;
//
//import org.platformlayer.model.Authentication;
//import org.platformlayer.ops.OpsException;
//import org.platformlayer.service.aptcache.model.AptCacheService;
//import org.platformlayer.ui.web.server.inject.PlatformLayerLiveObjects;
//import org.platformlayer.web.ContextMap;
//import org.platformlayer.xaas.services.ServiceProviderDictionary;
//import org.platformlayer.xaas.web.resources.ItemService;
//
//import com.google.web.bindery.requestfactory.server.RequestFactoryServlet;
//
//public class AptCacheServiceGwtService {
//
//    @Inject
//    ItemService itemService;
//
//    @Inject
//    ServiceProviderDictionary serviceProviderDictionary;
//
//    @Inject
//    Provider<ContextMap> contextMapProvider;
//
//    public List<AptCacheService> findAll() throws OpsException {
//        Authentication authentication = getAuthentication();
//
//        return itemService.findAll(authentication, AptCacheService.class);
//    }
//
//    public AptCacheService persist(AptCacheService managedItem) throws OpsException {
//        Authentication authentication = getAuthentication();
//
//        AptCacheService aptCacheService = itemService.createItem(authentication, managedItem);
//
//        PlatformLayerLiveObjects liveObjects = PlatformLayerLiveObjects.get(contextMapProvider);
//        liveObjects.notifyCreated(aptCacheService, aptCacheService.getId());
//
//        return aptCacheService;
//
//        // if (person.getId() == null) {
//        // person.setId(Long.toString(++serial));
//        // }
//        // person.setVersion(person.getVersion() + 1);
//        // if (person.getClassSchedule() != null) {
//        // scheduleStore.persist(person.getClassSchedule());
//        // }
//        // Person existing = people.get(person.getId());
//        // if (existing != null) {
//        // existing.copyFrom(person);
//        // } else {
//        // people.put(person.getId(), person);
//        // }
//    }
//
//    private Authentication getAuthentication() {
//        ContextMap contextMap = ContextMap.get(RequestFactoryServlet.getThreadLocalRequest());
//        Authentication authentication = contextMap.getTyped(Authentication.class);
//        return authentication;
//    }
// }
