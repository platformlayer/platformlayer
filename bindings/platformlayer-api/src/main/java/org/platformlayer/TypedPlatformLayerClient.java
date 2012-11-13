package org.platformlayer;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.platformlayer.common.UntypedItem;
import org.platformlayer.common.UntypedItemCollection;
import org.platformlayer.core.model.Action;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.ManagedItemCollection;
import org.platformlayer.core.model.ManagedItemState;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.ServiceInfo;
import org.platformlayer.core.model.Tag;
import org.platformlayer.core.model.TagChanges;
import org.platformlayer.core.model.Tags;
import org.platformlayer.ids.ManagedItemId;
import org.platformlayer.ids.ProjectId;
import org.platformlayer.jobs.model.JobData;
import org.platformlayer.jobs.model.JobDataList;
import org.platformlayer.jobs.model.JobExecutionData;
import org.platformlayer.jobs.model.JobExecutionList;
import org.platformlayer.jobs.model.JobLog;
import org.platformlayer.metrics.model.MetricDataStream;
import org.platformlayer.metrics.model.MetricInfoCollection;
import org.platformlayer.metrics.model.MetricQuery;
import org.platformlayer.ops.OpsException;
import org.platformlayer.xml.JaxbHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class TypedPlatformLayerClient implements PlatformLayerClient {
	private static final Logger log = LoggerFactory.getLogger(TypedPlatformLayerClient.class);

	final PlatformLayerClient platformLayerClient;
	final TypedItemMapper mapper;

	@Inject
	public TypedPlatformLayerClient(PlatformLayerClient platformLayerClient, TypedItemMapper mapper) {
		this.platformLayerClient = platformLayerClient;
		this.mapper = mapper;
	}

	public <T> T promoteToTyped(UntypedItem untypedItem) throws OpsException {
		return mapper.promoteToTyped(untypedItem);
	}

	public <T> T promoteToTyped(UntypedItem untypedItem, Class<T> itemClass) throws OpsException {
		return mapper.promoteToTyped(untypedItem, itemClass);
	}

	// public ItemBase getItem(String path) throws OpsException {
	// ItemBase item = findItem(path);
	// if (item == null)
	// throw new OpsException("Item not found: " + item);
	// return item;
	// }
	//
	// public ItemBase findItem(String path) throws OpsException {
	// UntypedItem cloudItemUntyped = platformLayerClient.getUntypedItem(path);
	// if (cloudItemUntyped == null) {
	// return null;
	// }
	//
	// return promoteToTyped(cloudItemUntyped);
	// }

	public <T> T getItem(PlatformLayerKey path, Class<T> itemClass) throws OpsException {
		T item = findItem(path, itemClass);
		if (item == null) {
			throw new OpsException("Item not found: " + path);
		}
		return item;
	}

	public <T> T getItem(PlatformLayerKey path) throws OpsException {
		T item = findItem(path);
		if (item == null) {
			throw new OpsException("Item not found: " + path);
		}
		return item;
	}

	public <T> T findItem(PlatformLayerKey path, Class<T> itemClass) throws OpsException {
		UntypedItem cloudItemUntyped = platformLayerClient.getItemUntyped(path);
		if (cloudItemUntyped == null) {
			return null;
		}

		return promoteToTyped(cloudItemUntyped, itemClass);
	}

	public <T> T findItem(String id, Class<T> itemClass) throws OpsException {
		PlatformLayerKey key = toKey(itemClass, id);

		UntypedItem item = platformLayerClient.getItemUntyped(key);
		if (item == null) {
			return null;
		}

		return promoteToTyped(item, itemClass);
	}

	public <T> PlatformLayerKey toKey(Class<T> itemClass, String id) throws PlatformLayerClientException {
		JaxbHelper jaxbHelper = PlatformLayerClientBase.toJaxbHelper(itemClass, new Class[0]);
		ManagedItemId itemId = new ManagedItemId(id);

		PlatformLayerKey key = PlatformLayerClientBase.toKey(jaxbHelper, itemId, itemClass,
				platformLayerClient.listServices(true));
		return key;
	}

	public <T> T findItem(PlatformLayerKey path) throws OpsException {
		UntypedItem cloudItemUntyped = platformLayerClient.getItemUntyped(path);
		if (cloudItemUntyped == null) {
			return null;
		}

		return promoteToTyped(cloudItemUntyped);
	}

	/**
	 * If using directly, consider using OwnedItem instead
	 */
	@Deprecated
	public <T extends ItemBase> T putItemByTag(T item, Tag uniqueTag) throws OpsException {
		JaxbHelper jaxbHelper = PlatformLayerClientBase.toJaxbHelper(item);

		String xml = PlatformLayerClientBase.serialize(jaxbHelper, item);
		PlatformLayerKey key = PlatformLayerClientBase.toKey(jaxbHelper, item, platformLayerClient.listServices(true));

		UntypedItem ret = platformLayerClient.putItemByTag(key, uniqueTag, xml, Format.XML);
		Class<T> itemClass = (Class<T>) item.getClass();
		return promoteToTyped(ret, itemClass);

	}

	/**
	 * Consider using putItemByTag instead (or OwnedItem) for idempotency
	 */
	@Deprecated
	public <T> T putItem(T item) throws OpsException {
		JaxbHelper jaxbHelper = PlatformLayerClientBase.toJaxbHelper(item);

		String xml = PlatformLayerClientBase.serialize(jaxbHelper, item);

		PlatformLayerKey key = PlatformLayerClientBase.toKey(jaxbHelper, item, platformLayerClient.listServices(true));

		UntypedItem created = platformLayerClient.putItem(key, xml, Format.XML);

		Class<T> itemClass = (Class<T>) item.getClass();
		return promoteToTyped(created, itemClass);
	}

	// public <T> Iterable<T> listItems(Class<T> itemClass) throws PlatformLayerClientException {
	// return platformLayerClient.listItems(itemClass);
	// }

	@Override
	public JobExecutionData deleteItem(PlatformLayerKey key) throws PlatformLayerClientException {
		return platformLayerClient.deleteItem(key);
	}

	public Tags addTags(PlatformLayerKey key, List<Tag> tags) throws PlatformLayerClientException {
		TagChanges changeTags = new TagChanges();

		changeTags.addTags.addAll(tags);

		return changeTags(key, changeTags);
	}

	public Tags addTag(PlatformLayerKey key, Tag tag) throws PlatformLayerClientException {
		return addTags(key, Collections.singletonList(tag));
	}

	public Tags addUniqueTag(PlatformLayerKey key, Tag tag) throws PlatformLayerClientException {
		// Sometimes we require idempotency; we can do this using unique tags.
		// TODO: Implement this
		log.warn("addUniqueTag not properly implemented");
		return addTag(key, tag);
	}

	// public <T> Iterable<T> listItems(Class<T> itemClass, Filter filter) throws PlatformLayerClientException {
	// return platformLayerClient.listItems(itemClass, filter);
	// }

	public <T> List<T> listItems(Class<T> clazz, boolean showDeleted) throws OpsException {
		Filter filter = showDeleted ? null : StateFilter.exclude(ManagedItemState.DELETED);
		return listItems(clazz, filter);
	}

	public <T> List<T> listItems(Class<T> clazz) throws OpsException {
		return listItems(clazz, false);
	}

	public <T> List<T> listItems(Class<T> clazz, PlatformLayerKey parent) throws OpsException {
		Filter filter = StateFilter.exclude(ManagedItemState.DELETED);
		filter = Filter.and(filter, TagFilter.byParent(parent));

		return listItems(clazz, filter);
	}

	public <T> List<T> listItems(Class<T> clazz, Filter filter) throws OpsException {
		JaxbHelper jaxbHelper = PlatformLayerClientBase.toJaxbHelper(clazz, ManagedItemCollection.class);
		PlatformLayerKey path = PlatformLayerClientBase.toKey(jaxbHelper, null, platformLayerClient.listServices(true));

		UntypedItemCollection untypedItems = this.platformLayerClient.listItemsUntyped(path);

		List<T> items = Lists.newArrayList();

		for (UntypedItem untypedItem : untypedItems.getItems()) {
			T item = promoteToTyped(untypedItem, clazz);
			items.add(item);
		}

		if (filter != null) {
			// TODO: Do filtering server-side
			List<T> filtered = Lists.newArrayList();
			for (T item : items) {
				if (filter.matches(item)) {
					filtered.add(item);
				}
			}
			return filtered;
		} else {
			return items;
		}
	}

	@Override
	public Tags changeTags(PlatformLayerKey key, TagChanges tagChanges) throws PlatformLayerClientException {
		return platformLayerClient.changeTags(key, tagChanges);
	}

	@Override
	public ProjectId getProject() {
		return platformLayerClient.getProject();
	}

	@Override
	public JobData doAction(PlatformLayerKey key, Action action) throws PlatformLayerClientException {
		return platformLayerClient.doAction(key, action);
	}

	/**
	 * Consider using putItemByTag instead (or OwnedItem) for idempotency
	 */
	@Deprecated
	@Override
	public UntypedItem putItem(PlatformLayerKey key, String data, Format dataFormat)
			throws PlatformLayerClientException {
		return platformLayerClient.putItem(key, data, dataFormat);
	}

	/**
	 * If using directly, consider using OwnedItem instead
	 */
	@Deprecated
	@Override
	public UntypedItem putItemByTag(PlatformLayerKey key, Tag uniqueTag, String data, Format dataFormat)
			throws PlatformLayerClientException {
		return platformLayerClient.putItemByTag(key, uniqueTag, data, dataFormat);
	}

	@Override
	public UntypedItem getItemUntyped(PlatformLayerKey key) throws PlatformLayerClientException {
		return platformLayerClient.getItemUntyped(key);
	}

	@Override
	public UntypedItemCollection listItemsUntyped(PlatformLayerKey path) throws PlatformLayerClientException {
		return platformLayerClient.listItemsUntyped(path);
	}

	@Override
	public UntypedItemCollection listRoots() throws PlatformLayerClientException {
		return platformLayerClient.listRoots();
	}

	@Override
	public UntypedItemCollection listChildren(PlatformLayerKey parent) throws PlatformLayerClientException {
		return platformLayerClient.listChildren(parent);
	}

	@Override
	public JobDataList listJobs() throws PlatformLayerClientException {
		return platformLayerClient.listJobs();
	}

	@Override
	public MetricDataStream getMetric(MetricQuery query) throws PlatformLayerClientException {
		return platformLayerClient.getMetric(query);
	}

	@Override
	public MetricInfoCollection listMetrics(PlatformLayerKey key) throws PlatformLayerClientException {
		return platformLayerClient.listMetrics(key);
	}

	@Override
	public Collection<ServiceInfo> listServices(boolean allowCache) throws PlatformLayerClientException {
		return platformLayerClient.listServices(allowCache);
	}

	@Override
	public String activateService(String serviceType, String data, Format format) throws PlatformLayerClientException {
		return platformLayerClient.activateService(serviceType, data, format);
	}

	@Override
	public String getActivation(String serviceType, Format format) throws PlatformLayerClientException {
		return platformLayerClient.getActivation(serviceType, format);
	}

	@Override
	public String getSshPublicKey(String serviceType) throws PlatformLayerClientException {
		return platformLayerClient.getSshPublicKey(serviceType);
	}

	@Override
	public String getSchema(String serviceType, Format format) throws PlatformLayerClientException {
		return platformLayerClient.getSchema(serviceType, format);
	}

	@Override
	public void ensureLoggedIn() throws PlatformLayerAuthenticationException {
		platformLayerClient.ensureLoggedIn();
	}

	@Override
	public PlatformLayerEndpointInfo getEndpointInfo(PlatformLayerKey item) {
		return platformLayerClient.getEndpointInfo(item);
	}

	@Override
	public JobLog getJobExecutionLog(String jobId, String executionId) throws PlatformLayerClientException {
		return platformLayerClient.getJobExecutionLog(jobId, executionId);
	}

	@Override
	public JobExecutionList listJobExecutions(PlatformLayerKey jobKey) throws PlatformLayerClientException {
		return platformLayerClient.listJobExecutions(jobKey);
	}

}
