package org.platformlayer.xaas.web.resources;

import java.util.List;

import org.platformlayer.Filter;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.auth.OpsAuthentication;

import com.google.inject.ImplementedBy;

@ImplementedBy(ItemServiceImpl.class)
public interface ItemService {
	// List<ManagedItem<Object>> listItems(ModelKey modelKey) throws RepositoryException;

	// <T> ManagedItem<T> createItem(Authentication auth, ModelKey modelKey, ManagedItem<T> untypedItem) throws
	// RepositoryException, OpsException;

	// TODO: Get Authentication by injection
	<T extends ItemBase> T createItem(OpsAuthentication auth, T typedItem) throws OpsException;

	<T extends ItemBase> T putItem(OpsAuthentication auth, T typedItem, String uniqueTag) throws OpsException;

	<T extends ItemBase> T findItem(OpsAuthentication auth, Class<T> itemClass, String id) throws OpsException;

	<T extends ItemBase> List<T> findAll(OpsAuthentication authentication, Class<T> itemClass) throws OpsException;

	<T extends ItemBase> PlatformLayerKey deleteItem(OpsAuthentication auth, PlatformLayerKey key) throws OpsException;

	List<ItemBase> findRoots(OpsAuthentication authentication) throws OpsException;

	List<ItemBase> listAll(OpsAuthentication authentication, Filter filter) throws OpsException;
}
