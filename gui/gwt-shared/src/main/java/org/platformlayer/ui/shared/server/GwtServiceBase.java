//package org.platformlayer.ui.shared.server;
//
//import java.util.List;
//
//import javax.inject.Inject;
//import javax.inject.Provider;
//
//import org.platformlayer.Scope;
//import org.platformlayer.core.model.ItemBase;
//import org.platformlayer.ops.OpsException;
//import org.platformlayer.ops.auth.OpsAuthentication;
//import org.platformlayer.ui.shared.server.inject.PlatformLayerLiveObjects;
//import org.platformlayer.xaas.web.resources.ItemService;
//
//public abstract class GwtServiceBase<T extends ItemBase> {
//
//	@Inject
//	ItemService itemService;
//
//	@Inject
//	Provider<Scope> scopeProvider;
//
//	@Inject
//	Provider<OpsAuthentication> authenticationProvider;
//
//	final Class<T> proxyClass;
//
//	public GwtServiceBase(Class<T> proxyClass) {
//		super();
//		this.proxyClass = proxyClass;
//	}
//
//	public List<T> findAll() throws OpsException {
//		OpsAuthentication authentication = authenticationProvider.get();
//
//		return itemService.findAll(authentication, proxyClass);
//	}
//
//	public T persist(T managedItem) throws OpsException {
//		OpsAuthentication authentication = authenticationProvider.get();
//
//		T created = itemService.createItem(authentication, managedItem);
//
//		PlatformLayerLiveObjects liveObjects = PlatformLayerLiveObjects.get();
//		liveObjects.notifyCreated(created, created.getId());
//
//		return created;
//	}
// }