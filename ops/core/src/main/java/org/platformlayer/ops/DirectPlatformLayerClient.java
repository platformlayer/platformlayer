package org.platformlayer.ops;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.platformlayer.Filter;
import org.platformlayer.Format;
import org.platformlayer.PlatformLayerAuthenticationException;
import org.platformlayer.PlatformLayerClientBase;
import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.PlatformLayerEndpointInfo;
import org.platformlayer.RepositoryException;
import org.platformlayer.TagFilter;
import org.platformlayer.TypedItemMapper;
import org.platformlayer.auth.crypto.SecretProvider;
import org.platformlayer.common.UntypedItem;
import org.platformlayer.common.UntypedItemCollection;
import org.platformlayer.core.model.Action;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.ServiceInfo;
import org.platformlayer.core.model.ServiceInfoCollection;
import org.platformlayer.core.model.Tag;
import org.platformlayer.core.model.TagChanges;
import org.platformlayer.core.model.Tags;
import org.platformlayer.ids.ItemType;
import org.platformlayer.ids.ProjectId;
import org.platformlayer.ids.ServiceType;
import org.platformlayer.jobs.model.JobData;
import org.platformlayer.jobs.model.JobDataList;
import org.platformlayer.jobs.model.JobExecutionList;
import org.platformlayer.jobs.model.JobLog;
import org.platformlayer.metrics.model.MetricDataStream;
import org.platformlayer.metrics.model.MetricInfoCollection;
import org.platformlayer.metrics.model.MetricQuery;
import org.platformlayer.model.ProjectAuthorization;
import org.platformlayer.xaas.repository.ManagedItemRepository;
import org.platformlayer.xaas.services.ModelClass;
import org.platformlayer.xaas.services.ServiceProvider;
import org.platformlayer.xaas.services.ServiceProviderDictionary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fathomdb.Casts;

public class DirectPlatformLayerClient extends PlatformLayerClientBase {

	private static final Logger log = LoggerFactory.getLogger(DirectPlatformLayerClient.class);

	private final ProjectId projectId;
	private final ProjectAuthorization auth;

	private final OpsSystem opsSystem;

	private Collection<ServiceInfo> services;

	@Inject
	ServiceProviderDictionary serviceProviderDictionary;

	@Inject
	ItemService itemService;

	@Inject
	ManagedItemRepository itemRepository;

	public DirectPlatformLayerClient(TypedItemMapper mapper, OpsSystem opsSystem, ProjectId projectId,
			ProjectAuthorization auth) {
		super(null);

		this.opsSystem = opsSystem;
		this.projectId = projectId;
		this.auth = auth;

		opsSystem.getInjector().injectMembers(this);
	}

	@Override
	public JobData doAction(PlatformLayerKey key, Action action) throws PlatformLayerClientException {
		throw new UnsupportedOperationException();
	}

	@Override
	public UntypedItem putItem(PlatformLayerKey key, String data, Format dataFormat)
			throws PlatformLayerClientException {
		throw new UnsupportedOperationException();
	}

	@Override
	public UntypedItem putItemByTag(PlatformLayerKey key, Tag uniqueTag, String data, Format dataFormat)
			throws PlatformLayerClientException {
		throw new UnsupportedOperationException();
	}

	@Override
	public JobData deleteItem(PlatformLayerKey key) throws PlatformLayerClientException {
		throw new UnsupportedOperationException();
	}

	@Override
	public UntypedItem getItemUntyped(PlatformLayerKey key) throws PlatformLayerClientException {
		throw new UnsupportedOperationException();
	}

	@Override
	public UntypedItemCollection listItemsUntyped(PlatformLayerKey path) throws PlatformLayerClientException {
		throw new UnsupportedOperationException();
	}

	@Override
	public UntypedItemCollection listRoots() throws PlatformLayerClientException {
		throw new UnsupportedOperationException();
	}

	@Override
	public UntypedItemCollection listChildren(PlatformLayerKey parent) throws PlatformLayerClientException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Tags changeTags(PlatformLayerKey key, TagChanges tagChanges, Long ifVersion)
			throws PlatformLayerClientException {
		ModelClass<?> modelClass = getModelClass(key.getServiceType(), key.getItemType());

		try {
			return itemRepository.changeTags(modelClass, getProject(), key.getItemId(), tagChanges, ifVersion);
		} catch (RepositoryException e) {
			throw new PlatformLayerClientException("Error changing tags", e);
		}
	}

	protected ModelClass<?> getModelClass(ServiceType serviceType, ItemType itemType)
			throws PlatformLayerClientException {
		ServiceProvider serviceProvider = getServiceProvider(serviceType);
		if (serviceProvider == null) {
			log.warn("Unknown serviceType: " + serviceType);
			throw new PlatformLayerClientException("Service type not recognized: " + serviceType.getKey());
		}
		ModelClass<?> modelClass = serviceProvider.getModels().find(itemType);
		if (modelClass == null) {
			log.warn("Unknown itemtype: " + itemType);
			throw new PlatformLayerClientException("Item type not recognized: " + itemType.getKey());
		}

		return modelClass;
	}

	protected ServiceProvider getServiceProvider(ServiceType serviceType) {
		return serviceProviderDictionary.getServiceProvider(serviceType);
	}

	@Override
	public JobDataList listJobs() throws PlatformLayerClientException {
		throw new UnsupportedOperationException();
	}

	@Override
	public JobLog getJobExecutionLog(String jobId, String executionId) throws PlatformLayerClientException {
		throw new UnsupportedOperationException();
	}

	@Override
	public MetricDataStream getMetric(MetricQuery query) throws PlatformLayerClientException {
		throw new UnsupportedOperationException();
	}

	@Override
	public MetricInfoCollection listMetrics(PlatformLayerKey key) throws PlatformLayerClientException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<ServiceInfo> listServices(boolean allowCache) throws PlatformLayerClientException {
		Collection<ServiceInfo> services = this.services;

		if (!allowCache || services == null) {
			ServiceInfoCollection serviceInfoCollection = opsSystem.listServices();
			services = serviceInfoCollection.services;
			this.services = services;
		}

		return services;
	}

	@Override
	public String activateService(String serviceType, String data, Format format) throws PlatformLayerClientException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getActivation(String serviceType, Format format) throws PlatformLayerClientException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getSshPublicKey(String serviceType) throws PlatformLayerClientException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getSchema(String serviceType, Format format) throws PlatformLayerClientException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void ensureLoggedIn() throws PlatformLayerAuthenticationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public ProjectId getProject() {
		return projectId;
	}

	@Override
	public PlatformLayerEndpointInfo getEndpointInfo(PlatformLayerKey item) {
		throw new UnsupportedOperationException();
	}

	@Override
	public JobExecutionList listJobExecutions(String jobId) throws PlatformLayerClientException {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> List<T> listItems(Class<T> clazz) throws PlatformLayerClientException, OpsException {
		Class<? extends ItemBase> itemClass = (Class<? extends ItemBase>) clazz;
		List<T> listItems = (List<T>) itemService.findAll(auth, itemClass);
		return listItems;
	}

	@Override
	public <T extends ItemBase> T putItemByTag(T item, Tag uniqueTag) throws OpsException {
		return itemService.putItem(auth, item, uniqueTag.getKey());
	}

	@Override
	public <T extends ItemBase> T putItem(T item) throws OpsException {
		return itemService.putItem(auth, item, null);
	}

	@Override
	public List<ItemBase> listChildrenTyped(PlatformLayerKey parentKey) throws OpsException {
		Tag parentTag = Tag.buildParentTag(parentKey);
		Filter filter = TagFilter.byTag(parentTag);
		List<ItemBase> items = itemService.listAll(auth, filter);
		return items;
	}

	@Override
	public <T> T findItem(PlatformLayerKey key, Class<T> itemClass) throws OpsException {
		ItemBase managedItem = findItem(key);
		return Casts.checkedCast(managedItem, itemClass);
	}

	@Override
	public <T> T findItem(PlatformLayerKey key) throws OpsException {
		boolean fetchTags = true;
		ItemBase managedItem;
		try {
			managedItem = itemRepository.getManagedItem(key, fetchTags, getSecretProvider());
		} catch (RepositoryException e) {
			throw new PlatformLayerClientException("Error fetching item", e);
		}
		return (T) managedItem;
	}

	protected SecretProvider getSecretProvider() {
		return SecretProvider.from(auth);
	}

}
