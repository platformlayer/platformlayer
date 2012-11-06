package org.platformlayer.xaas.web.resources;

import java.util.List;

import javax.inject.Inject;

import org.platformlayer.CastUtils;
import org.platformlayer.CheckedCallable;
import org.platformlayer.Filter;
import org.platformlayer.RepositoryException;
import org.platformlayer.StateFilter;
import org.platformlayer.TagFilter;
import org.platformlayer.auth.crypto.SecretProvider;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.ManagedItemState;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Tag;
import org.platformlayer.ids.ManagedItemId;
import org.platformlayer.ids.ProjectId;
import org.platformlayer.ids.ServiceType;
import org.platformlayer.model.ProjectAuthorization;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsSystem;
import org.platformlayer.ops.tasks.OpsContextBuilder;
import org.platformlayer.xaas.repository.ManagedItemRepository;
import org.platformlayer.xaas.services.ChangeQueue;
import org.platformlayer.xaas.services.ModelClass;
import org.platformlayer.xaas.services.ServiceProvider;
import org.platformlayer.xaas.services.ServiceProviderDictionary;
import org.platformlayer.xml.JaxbHelper;
import org.platformlayer.xml.JsonHelper;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.inject.Injector;

public class ItemServiceImpl implements ItemService {
	@Inject
	protected ManagedItemRepository repository;

	@Inject
	protected ChangeQueue changeQueue;

	@Inject
	Injector injector;

	@Inject
	OpsSystem opsSystem;

	@Inject
	ServiceProviderDictionary serviceProviderDirectory;

	// @Override
	// public List<ManagedItem<Object>> listItems(ModelKey modelKey) throws RepositoryException {
	// boolean fetchTags = true;
	// List<ManagedItem<Object>> items = repository.getByAccountId(modelKey, fetchTags);
	// return items;
	// // ManagedItemCollection<T> collection = new ManagedItemCollection<T>();
	// // collection.items = items;
	// // return collection;
	// }

	@Override
	public <T extends ItemBase> List<T> findAll(ProjectAuthorization authentication, Class<T> itemClass)
			throws OpsException {
		ModelClass<T> modelClass = serviceProviderDirectory.getModelClass(itemClass);

		// if (modelClass.isSystemObject()) {
		// if (!isInRole(authentication, RoleId.ADMIN)) {
		// throw new SecurityException();
		// }
		// }

		// Class<T> javaClass = modelClass.getJavaClass();

		ProjectId project = getProjectId(authentication);

		// ModelKey modelKey = new ModelKey(modelClass.getServiceType(), modelClass.getItemType(), project, null);

		// if (isSystemObject(modelKey)) {
		// checkLoggedInAsAdmin();
		// }

		boolean fetchTags = true;
		List<T> items;
		try {
			items = repository.findAll(modelClass, project, fetchTags, getSecretProvider(authentication), Filter.EMPTY);
		} catch (RepositoryException e) {
			throw new OpsException("Error reading objects from database", e);
		}
		return items;
		//
		// List<ManagedItem<Object>> listItems = itemService.listItems(modelKey);
		//
		// ServiceProvider serviceProvider = serviceProviderDictionary.getServiceProvider(serviceType);
		// Class<?> javaClass = serviceProvider.getJavaClass(itemType);
		//
		// List<AptCacheService> typedItems = Lists.newArrayList();
		// for (ManagedItem<Object> item : listItems) {
		// ModelKey itemKey = new ModelKey(serviceType, itemType, project, new ManagedItemId(item.getKey()));
		// TypedManagedItem<?> typedManagedItem = TypedManagedItem.build(javaClass, itemKey, item);
		// typedItems.add((AptCacheService) typedManagedItem.getModel());
		// }
		//
		// return typedItems;
	}

	private SecretProvider getSecretProvider(ProjectAuthorization authz) {
		return SecretProvider.from(authz);
	}

	// @Override
	// public <T> ManagedItem<T> createItem(Authentication auth, ModelKey modelKey, ManagedItem<T> untypedItem) throws
	// RepositoryException, OpsException {
	// // ModelKey modelKey = item.getModelKey();
	// ServiceProvider serviceProvider = opsSystem.getServiceProvider(modelKey.getServiceType());
	//
	// Class<T> javaClass = (Class<T>) opsSystem.getJavaClass(modelKey);
	//
	// TypedManagedItem<T> item = TypedManagedItem.build(javaClass, modelKey, untypedItem);
	// // ModelClass modelClass = item.getModelClass();
	// // ServiceProvider serviceProvider = modelClass.getProvider();
	//
	// item.setState(ManagedItemState.CREATION_REQUESTED);
	// String modelData = item.getModelData();
	// if (Strings.isNullOrEmpty(modelData))
	// throw new IllegalArgumentException("ModelData is required");
	//
	// modelData = modelData.trim();
	// if (modelData.startsWith("{")) {
	// // Convert JSON to XML
	// JsonHelper<?> jsonHelper = getJsonHelper(modelKey);
	//
	// String json = jsonHelper.wrapJson(modelData);
	//
	// Object model;
	// try {
	// jsonHelper.addDefaultNamespace();
	// model = jsonHelper.unmarshal(json);
	// } catch (Exception e) {
	// throw new IllegalArgumentException("Could not parse model data", e);
	// }
	// JaxbHelper jaxbHelper = getJaxbHelper(modelKey);
	// String xml;
	// try {
	// xml = jaxbHelper.marshal(model, false);
	// } catch (JAXBException e) {
	// throw new IllegalArgumentException("Could not convert JSON to XML", e);
	// }
	// item.setModelData(xml);
	// }
	//
	// serviceProvider.beforeCreateItem(item);
	//
	// {
	// // Set the model data, in case we have changed it
	// JaxbHelper jaxbHelper = getJaxbHelper(modelKey);
	// String xml;
	// try {
	// xml = jaxbHelper.marshal(item.getModel(), false);
	// } catch (JAXBException e) {
	// throw new IllegalArgumentException("Could not convert JSON to XML", e);
	// }
	// item.setModelData(xml);
	// }
	//
	// // ProjectId project = new ProjectId(auth.getProject());
	//
	// ManagedItem created = repository.createManagedItem(modelKey, item.getSerialized());
	//
	// changeQueue.notifyChange(serviceProvider.getServiceType(), auth, item);
	//
	// return item.getSerialized();
	// }

	// protected boolean isInRole(ProjectAuthorization auth, RoleId role) {
	// return auth.isInRole(auth.getProjectId(), role);
	// }

	private JsonHelper<?> getJsonHelper(PlatformLayerKey modelKey) {
		Class<?> javaClass = opsSystem.getJavaClass(modelKey);
		return JsonHelper.build(javaClass);
	}

	private JaxbHelper getJaxbHelper(PlatformLayerKey modelKey) {
		Class<?> javaClass = opsSystem.getJavaClass(modelKey);
		return JaxbHelper.get(javaClass);
	}

	@Override
	public <T extends ItemBase> T createItem(final ProjectAuthorization auth, final T item, boolean generateUniqueName)
			throws OpsException {
		return ensureItem(auth, item, false, generateUniqueName, null);
	}

	<T extends ItemBase> T ensureItem(final ProjectAuthorization auth, final T item, final boolean canExist,
			final boolean generateUniqueName, final String uniqueTagKey) throws OpsException {
		final ModelClass<T> modelClass = (ModelClass<T>) serviceProviderDirectory.getModelClass(item.getClass());
		if (modelClass == null) {
			throw new IllegalStateException("Unknown item type");
		}
		final Class<T> javaClass = modelClass.getJavaClass();

		// JaxbHelper jaxbHelper = JaxbHelper.get(javaClass);

		String id = item.getId();

		if (Strings.isNullOrEmpty(id)) {
			if (generateUniqueName) {
				// TODO: Try to build something based on the values??
				id = modelClass.getItemType().getKey();
			} else {
				// TODO: We could auto-generate this, but it seems better to require it,
				// otherwise we end up with lots of randomly named items
				throw new OpsException("Must specify item id");
				// id = UUID.randomUUID().toString();
				// item.setId(id);
			}
		}

		ProjectId project = getProjectId(auth);
		PlatformLayerKey itemKey = new PlatformLayerKey(null, project, modelClass.getServiceType(),
				modelClass.getItemType(), new ManagedItemId(id));
		item.setKey(itemKey);

		item.state = ManagedItemState.CREATION_REQUESTED;

		final ServiceProvider serviceProvider = modelClass.getProvider();

		final OpsContext opsContext = buildTemporaryOpsContext(modelClass.getServiceType(), auth);

		T created = OpsContext.runInContext(opsContext, new CheckedCallable<T, Exception>() {
			@Override
			public T call() throws Exception {
				PlatformLayerKey itemKey = item.getKey();

				T existing;

				SecretProvider secretProvider = SecretProvider.from(auth);

				if (uniqueTagKey != null) {
					boolean fetchTags = true;
					Tag uniqueTag = null;
					for (Tag tag : item.getTags()) {
						if (Objects.equal(tag.getKey(), uniqueTagKey)) {
							uniqueTag = tag;
						}
					}
					if (uniqueTag == null) {
						throw new IllegalArgumentException("Could not find unique tag");
					}
					Filter filter = TagFilter.byTag(uniqueTag);
					filter = Filter.and(filter, StateFilter.exclude(ManagedItemState.DELETED));

					existing = null;
					List<T> existingList = repository.findAll(modelClass, itemKey.getProject(), fetchTags,
							secretProvider, filter);
					if (!existingList.isEmpty()) {
						if (existingList.size() != 1) {
							throw new IllegalArgumentException("Found multiple items with unique tag");
						}
						existing = existingList.get(0);
					}

					if (existing == null) {
						itemKey = findUniqueId(item, itemKey, secretProvider);
					}
				} else {
					if (generateUniqueName) {
						itemKey = findUniqueId(item, itemKey, secretProvider);
					}

					try {
						boolean fetchTags = true;
						existing = CastUtils.checkedCast(repository.getManagedItem(itemKey, fetchTags, secretProvider),
								javaClass);
					} catch (RepositoryException e) {
						throw new OpsException("Error fetching item from database", e);
					}
				}

				if (!canExist && existing != null) {
					throw new OpsException("Item already exists");
				}

				serviceProvider.beforeCreateItem(item);

				ProjectId project = getProjectId(auth);
				T newItem;
				try {
					if (existing == null) {
						newItem = repository.createManagedItem(project, item);
					} else {
						item.secret = existing.secret;
						item.setKey(existing.getKey());

						newItem = repository.updateManagedItem(project, item);
					}
				} catch (RepositoryException e) {
					throw new OpsException("Error writing object to database", e);
				}

				itemKey = newItem.getKey();
				changeQueue.notifyChange(auth, itemKey, ManagedItemState.CREATION_REQUESTED);

				return newItem;
			}

			private <T extends ItemBase> PlatformLayerKey findUniqueId(final T item, final PlatformLayerKey itemKey,
					SecretProvider secretProvider) throws RepositoryException {
				int sequence = 0;
				while (true) {
					String tryId = item.getId();
					if (sequence != 0) {
						tryId += sequence;
					}
					final PlatformLayerKey tryKey = itemKey.withId(new ManagedItemId(tryId));
					boolean fetchTags = false;
					ItemBase found = repository.getManagedItem(tryKey, fetchTags, secretProvider);
					if (found == null) {
						item.setKey(tryKey);
						return tryKey;
					}
					sequence++;
				}
			}
		});

		return created;
	}

	@Override
	public <T extends ItemBase> T putItem(final ProjectAuthorization auth, final T item, String uniqueTag)
			throws OpsException {
		boolean generateUniqueName = false;
		return ensureItem(auth, item, true, generateUniqueName, uniqueTag);
	}

	protected OpsContext buildTemporaryOpsContext(ServiceType serviceType, ProjectAuthorization auth)
			throws OpsException {
		OpsContextBuilder opsContextBuilder = injector.getInstance(OpsContextBuilder.class);
		return opsContextBuilder.buildTemporaryOpsContext(serviceType, auth);
	}

	@Override
	public PlatformLayerKey deleteItem(final ProjectAuthorization auth, final PlatformLayerKey targetItemKey)
			throws OpsException {
		SecretProvider secretProvider = SecretProvider.from(auth);

		boolean fetchTags = true;
		ItemBase targetItem;
		try {
			targetItem = repository.getManagedItem(targetItemKey, fetchTags, secretProvider);
		} catch (RepositoryException e) {
			throw new OpsException("Error reading item", e);
		}

		if (targetItem == null) {
			throw new IllegalStateException("Item not found");
		}

		targetItem.state = ManagedItemState.DELETE_REQUESTED;

		final ServiceProvider serviceProvider = serviceProviderDirectory.getServiceProvider(targetItemKey
				.getServiceType());
		if (serviceProvider == null) {
			throw new IllegalStateException("Unknown service type");
		}

		final OpsContext opsContext = buildTemporaryOpsContext(targetItemKey.getServiceType(), auth);

		PlatformLayerKey jobKey = OpsContext.runInContext(opsContext,
				new CheckedCallable<PlatformLayerKey, Exception>() {
					@Override
					public PlatformLayerKey call() throws Exception {
						try {
							repository.changeState(targetItemKey, ManagedItemState.DELETE_REQUESTED);
						} catch (RepositoryException e) {
							throw new OpsException("Error writing object to database", e);
						}

						return changeQueue.notifyChange(auth, targetItemKey, ManagedItemState.DELETE_REQUESTED);
					}
				});
		return jobKey;
	}

	@Override
	public <T extends ItemBase> T findItem(ProjectAuthorization auth, Class<T> itemClass, String id)
			throws OpsException {
		ModelClass<T> modelClass = serviceProviderDirectory.getModelClass(itemClass);
		// Class<T> javaClass = modelClass.getJavaClass();

		ProjectId project = getProjectId(auth);

		PlatformLayerKey modelKey = new PlatformLayerKey(null, project, modelClass.getServiceType(),
				modelClass.getItemType(), new ManagedItemId(id));

		boolean fetchTags = true;
		T managedItem;
		try {
			managedItem = CastUtils.checkedCast(
					repository.getManagedItem(modelKey, fetchTags, SecretProvider.from(auth)), itemClass);
		} catch (RepositoryException e) {
			throw new OpsException("Error fetching item from database", e);
		}

		return managedItem;
	}

	@Override
	public List<ItemBase> findRoots(ProjectAuthorization authentication) throws OpsException {
		ProjectId project = getProjectId(authentication);

		boolean fetchTags = true;
		List<ItemBase> items;
		try {
			items = repository.findRoots(project, fetchTags, SecretProvider.from(authentication));
		} catch (RepositoryException e) {
			throw new OpsException("Error reading objects from database", e);
		}
		return items;
	}

	@Override
	public List<ItemBase> listAll(ProjectAuthorization authentication, Filter filter) throws OpsException {
		ProjectId project = getProjectId(authentication);

		List<ItemBase> items;
		try {
			items = repository.listAll(project, filter, SecretProvider.from(authentication));
		} catch (RepositoryException e) {
			throw new OpsException("Error reading objects from database", e);
		}
		return items;
	}

	private ProjectId getProjectId(ProjectAuthorization authentication) {
		String key = authentication.getName();
		if (key == null) {
			throw new IllegalStateException();
		}
		return new ProjectId(key);
	}

	// public void deleteItem(Authentication auth, TypedManagedItem<?> item) throws RepositoryException, OpsException {
	// ModelKey modelKey = item.getModelKey();
	// ServiceProvider serviceProvider = opsSystem.getServiceProvider(modelKey.getServiceType());
	//
	// item.setState(ManagedItemState.DELETE_REQUESTED);
	// serviceProvider.beforeDeleteItem(item);
	//
	// changeQueue.notifyChange(serviceProvider.getServiceType(), auth, item);
	// }
}
