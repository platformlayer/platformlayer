package org.platformlayer.xaas.web.resources;

import java.util.List;

import org.platformlayer.Filter;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.jobs.model.JobExecutionData;
import org.platformlayer.model.ProjectAuthorization;
import org.platformlayer.ops.OpsException;

import com.google.inject.ImplementedBy;

@ImplementedBy(ItemServiceImpl.class)
public interface ItemService {
	// List<ManagedItem<Object>> listItems(ModelKey modelKey) throws RepositoryException;

	// <T> ManagedItem<T> createItem(Authentication auth, ModelKey modelKey, ManagedItem<T> untypedItem) throws
	// RepositoryException, OpsException;

	// TODO: Get Authentication by injection
	<T extends ItemBase> T createItem(ProjectAuthorization auth, T typedItem, boolean generateUniqueName)
			throws OpsException;

	<T extends ItemBase> T putItem(ProjectAuthorization auth, T typedItem, String uniqueTag) throws OpsException;

	<T extends ItemBase> T findItem(ProjectAuthorization auth, Class<T> itemClass, String id) throws OpsException;

	<T extends ItemBase> List<T> findAll(ProjectAuthorization authentication, Class<T> itemClass) throws OpsException;

	<T extends ItemBase> JobExecutionData deleteItem(ProjectAuthorization auth, PlatformLayerKey key)
			throws OpsException;

	List<ItemBase> findRoots(ProjectAuthorization authentication) throws OpsException;

	List<ItemBase> listAll(ProjectAuthorization authentication, Filter filter) throws OpsException;
}
