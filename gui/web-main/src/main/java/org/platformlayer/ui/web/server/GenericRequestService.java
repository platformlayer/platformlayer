//package org.platformlayer.ui.web.server;
//
//import java.util.List;
//
//import javax.inject.Inject;
//import javax.inject.Provider;
//
//import org.platformlayer.core.model.ItemBase;
//import org.platformlayer.model.Authentication;
//import org.platformlayer.ops.OpsException;
//import org.platformlayer.ui.web.server.inject.PlatformLayerLiveObjects;
//import org.platformlayer.web.ContextMap;
//import org.platformlayer.xaas.web.resources.ItemService;
//
//public class GenericRequestService {
//
//    @Inject
//    ItemService itemService;
//
//    @Inject
//    Provider<ContextMap> contextMapProvider;
//
//    @Inject
//    Provider<Authentication> authenticationProvider;
//
//    public <T extends ItemBase> List<T> findAll(Class<T> proxyClass) throws OpsException {
//        Authentication authentication = authenticationProvider.get();
//
//        return itemService.findAll(authentication, proxyClass);
//    }
//
//    public <T extends ItemBase> T persist(T managedItem) throws OpsException {
//        Authentication authentication = authenticationProvider.get();
//
//        T created = itemService.createItem(authentication, managedItem);
//
//        PlatformLayerLiveObjects liveObjects = PlatformLayerLiveObjects.get(contextMapProvider);
//        liveObjects.notifyCreated(created, created.getId());
//
//        return created;
//    }
// }