package org.platformlayer.xaas.web.resources;

import java.util.List;

import javax.inject.Inject;

import org.platformlayer.CastUtils;
import org.platformlayer.CheckedCallable;
import org.platformlayer.Filter;
import org.platformlayer.RepositoryException;
import org.platformlayer.Strings;
import org.platformlayer.TagFilter;
import org.platformlayer.auth.crypto.SecretProvider;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.ManagedItemState;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Tag;
import org.platformlayer.ids.ManagedItemId;
import org.platformlayer.ids.ProjectId;
import org.platformlayer.ids.ServiceType;
import org.platformlayer.model.RoleId;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsSystem;
import org.platformlayer.ops.auth.OpsAuthentication;
import org.platformlayer.ops.tasks.OpsContextBuilder;
import org.platformlayer.xaas.repository.ManagedItemRepository;
import org.platformlayer.xaas.services.ChangeQueue;
import org.platformlayer.xaas.services.ModelClass;
import org.platformlayer.xaas.services.ServiceProvider;
import org.platformlayer.xaas.services.ServiceProviderDictionary;
import org.platformlayer.xml.JaxbHelper;
import org.platformlayer.xml.JsonHelper;

import com.google.common.base.Objects;
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
	public <T extends ItemBase> List<T> findAll(OpsAuthentication authentication, Class<T> itemClass)
			throws OpsException {
		ModelClass<T> modelClass = serviceProviderDirectory.getModelClass(itemClass);

		if (modelClass.isSystemObject()) {
			if (!isInRole(authentication, RoleId.ADMIN)) {
				throw new SecurityException();
			}
		}

		// Class<T> javaClass = modelClass.getJavaClass();

		ProjectId project = authentication.getProjectId();

		// ModelKey modelKey = new ModelKey(modelClass.getServiceType(), modelClass.getItemType(), project, null);

		// if (isSystemObject(modelKey)) {
		// checkLoggedInAsAdmin();
		// }

		boolean fetchTags = true;
		List<T> items;
		try {
			items = repository.findAll(modelClass, project, fetchTags, SecretProvider.withAuth(authentication),
					Filter.EMPTY);
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

	protected boolean isInRole(OpsAuthentication auth, RoleId role) {
		return auth.isInRole(auth.getProjectId(), role);
	}

	private JsonHelper<?> getJsonHelper(PlatformLayerKey modelKey) {
		Class<?> javaClass = opsSystem.getJavaClass(modelKey);
		return JsonHelper.build(javaClass);
	}

	private JaxbHelper getJaxbHelper(PlatformLayerKey modelKey) {
		Class<?> javaClass = opsSystem.getJavaClass(modelKey);
		return JaxbHelper.get(javaClass);
	}

	@Override
	public <T extends ItemBase> T createItem(final OpsAuthentication auth, final T item) throws OpsException {
		return ensureItem(auth, item, false, null);
	}

	<T extends ItemBase> T ensureItem(final OpsAuthentication auth, final T item, final boolean canExist,
			final String uniqueTagKey) throws OpsException {
		final ModelClass<T> modelClass = (ModelClass<T>) serviceProviderDirectory.getModelClass(item.getClass());
		if (modelClass == null) {
			throw new IllegalStateException("Unknown item type");
		}
		final Class<T> javaClass = modelClass.getJavaClass();

		// JaxbHelper jaxbHelper = JaxbHelper.get(javaClass);

		String id = item.getId();
		if (Strings.isEmpty(id)) {
			// TODO: We could auto-generate this, but it seems better to require it,
			// otherwise we end up with lots of randomly named items
			throw new OpsException("Must specify item id");
			// id = UUID.randomUUID().toString();
			// item.setId(id);
		}

		ProjectId project = auth.getProjectId();

		final PlatformLayerKey modelKey = new PlatformLayerKey(null, project, modelClass.getServiceType(),
				modelClass.getItemType(), new ManagedItemId(id));

		item.state = ManagedItemState.CREATION_REQUESTED;

		final ServiceProvider serviceProvider = modelClass.getProvider();

		final OpsContext opsContext = buildTemporaryOpsContext(modelClass.getServiceType(), auth);

		T created = OpsContext.runInContext(opsContext, new CheckedCallable<T, Exception>() {
			@Override
			public T call() throws Exception {
				T existing;

				SecretProvider secretProvider = SecretProvider.withAuth(auth);

				if (uniqueTagKey != null) {
					boolean fetchTags = true;
					Tag uniqueTag = null;
					for (Tag tag : item.getTags()) {
						if (Objects.equal(tag.key, uniqueTagKey)) {
							uniqueTag = tag;
						}
					}
					if (uniqueTag == null) {
						throw new IllegalArgumentException("Could not find unique tag");
					}
					Filter filter = TagFilter.byTag(uniqueTag);

					existing = null;
					List<T> existingList = repository.findAll(modelClass, modelKey.getProject(), fetchTags,
							secretProvider, filter);
					if (!existingList.isEmpty()) {
						if (existingList.size() != 1) {
							throw new IllegalArgumentException("Found multiple items with unique tag");
						}
						existing = existingList.get(0);
					}

					if (existing == null) {
						int sequence = 0;
						while (true) {
							String tryId = item.getId();
							if (sequence != 0) {
								tryId += sequence;
							}
							final PlatformLayerKey tryKey = modelKey.withId(new ManagedItemId(tryId));
							ItemBase found = repository.getManagedItem(tryKey, fetchTags, secretProvider);
							if (found == null) {
								item.setKey(tryKey);
								break;
							}
							sequence++;
						}
					}
				} else {
					try {
						boolean fetchTags = true;
						existing = CastUtils.checkedCast(
								repository.getManagedItem(modelKey, fetchTags, secretProvider), javaClass);
					} catch (RepositoryException e) {
						throw new OpsException("Error fetching item from database", e);
					}
				}

				if (!canExist && existing != null) {
					throw new OpsException("Item already exists");
				}

				serviceProvider.beforeCreateItem(item);

				ProjectId project = auth.getProjectId();
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

				PlatformLayerKey itemKey = newItem.getKey();
				changeQueue.notifyChange(auth, itemKey, ManagedItemState.CREATION_REQUESTED);

				return newItem;
			}
		});

		return created;
	}

	@Override
	public <T extends ItemBase> T putItem(final OpsAuthentication auth, final T item, String uniqueTag)
			throws OpsException {
		return ensureItem(auth, item, true, uniqueTag);
	}

	protected OpsContext buildTemporaryOpsContext(ServiceType serviceType, OpsAuthentication auth) throws OpsException {
		OpsContextBuilder opsContextBuilder = injector.getInstance(OpsContextBuilder.class);
		return opsContextBuilder.buildTemporaryOpsContext(serviceType, auth);
	}

	@Override
	public void deleteItem(final OpsAuthentication auth, final PlatformLayerKey key) throws OpsException {
		SecretProvider secretProvider = SecretProvider.withAuth(auth);

		boolean fetchTags = true;
		ItemBase item;
		try {
			item = repository.getManagedItem(key, fetchTags, secretProvider);
		} catch (RepositoryException e) {
			throw new OpsException("Error reading item", e);
		}

		if (item == null) {
			throw new IllegalStateException("Item not found");
		}

		item.state = ManagedItemState.DELETE_REQUESTED;

		final ServiceProvider serviceProvider = serviceProviderDirectory.getServiceProvider(key.getServiceType());
		if (serviceProvider == null) {
			throw new IllegalStateException("Unknown service type");
		}

		final OpsContext opsContext = buildTemporaryOpsContext(key.getServiceType(), auth);

		OpsContext.runInContext(opsContext, new CheckedCallable<Object, Exception>() {
			@Override
			public Object call() throws Exception {
				try {
					repository.changeState(key, ManagedItemState.DELETE_REQUESTED);
				} catch (RepositoryException e) {
					throw new OpsException("Error writing object to database", e);
				}

				changeQueue.notifyChange(auth, key, ManagedItemState.DELETE_REQUESTED);
				return null;
			}
		});
	}

	@Override
	public <T extends ItemBase> T findItem(OpsAuthentication auth, Class<T> itemClass, String id) throws OpsException {
		ModelClass<T> modelClass = serviceProviderDirectory.getModelClass(itemClass);
		// Class<T> javaClass = modelClass.getJavaClass();

		ProjectId project = auth.getProjectId();

		PlatformLayerKey modelKey = new PlatformLayerKey(null, project, modelClass.getServiceType(),
				modelClass.getItemType(), new ManagedItemId(id));

		boolean fetchTags = true;
		T managedItem;
		try {
			managedItem = CastUtils.checkedCast(
					repository.getManagedItem(modelKey, fetchTags, SecretProvider.withAuth(auth)), itemClass);
		} catch (RepositoryException e) {
			throw new OpsException("Error fetching item from database", e);
		}

		return managedItem;
	}

	@Override
	public List<ItemBase> findRoots(OpsAuthentication authentication) throws OpsException {
		ProjectId project = authentication.getProjectId();

		boolean fetchTags = true;
		List<ItemBase> items;
		try {
			items = repository.findRoots(project, fetchTags, SecretProvider.withAuth(authentication));
		} catch (RepositoryException e) {
			throw new OpsException("Error reading objects from database", e);
		}
		return items;
	}

	@Override
	public List<ItemBase> listAll(OpsAuthentication authentication, Filter filter) throws OpsException {
		ProjectId project = authentication.getProjectId();

		List<ItemBase> items;
		try {
			items = repository.listAll(project, filter, SecretProvider.withAuth(authentication));
		} catch (RepositoryException e) {
			throw new OpsException("Error reading objects from database", e);
		}
		return items;
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
