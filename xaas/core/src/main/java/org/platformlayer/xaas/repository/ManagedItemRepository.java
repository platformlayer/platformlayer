package org.platformlayer.xaas.repository;

import java.util.List;

import org.platformlayer.Filter;
import org.platformlayer.RepositoryException;
import org.platformlayer.auth.crypto.SecretProvider;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.ManagedItemState;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.TagChanges;
import org.platformlayer.core.model.Tags;
import org.platformlayer.ids.ManagedItemId;
import org.platformlayer.ids.ProjectId;
import org.platformlayer.xaas.services.ModelClass;

public interface ManagedItemRepository {
    // <T> RepositoryQuery<T> buildQuery(ModelClass<T> modelClass);

    // <T> List<ManagedItem<T>> getByAccountId(ModelKey modelKey, boolean fetchTags) throws RepositoryException;

    ItemBase getManagedItem(PlatformLayerKey key, boolean fetchTags, SecretProvider secretProvider) throws RepositoryException;

    Tags changeTags(ModelClass<?> modelClass, ProjectId projectId, ManagedItemId itemId, TagChanges changeTags) throws RepositoryException;

    <T extends ItemBase> T createManagedItem(ProjectId project, T item) throws RepositoryException;

    <T extends ItemBase> T updateManagedItem(ProjectId project, T item) throws RepositoryException;

    <T> void changeState(PlatformLayerKey key, ManagedItemState newState) throws RepositoryException;

    <T extends ItemBase> List<T> findAll(ModelClass<T> modelClass, ProjectId project, boolean fetchTags, SecretProvider secretProvider, Filter filter) throws RepositoryException;

    List<ItemBase> findRoots(ProjectId project, boolean fetchTags, SecretProvider secretProvider) throws RepositoryException;

    List<ItemBase> listAll(ProjectId project, Filter filter, SecretProvider secretProvider) throws RepositoryException;

    // <T> T persist(Managed<T> managedItem);
}
