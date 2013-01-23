package org.platformlayer;

import java.util.Collection;
import java.util.List;

import org.platformlayer.common.UntypedItem;
import org.platformlayer.common.UntypedItemCollection;
import org.platformlayer.core.model.Action;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.ServiceInfo;
import org.platformlayer.core.model.Tag;
import org.platformlayer.core.model.TagChanges;
import org.platformlayer.core.model.Tags;
import org.platformlayer.ids.ProjectId;
import org.platformlayer.jobs.model.JobData;
import org.platformlayer.jobs.model.JobDataList;
import org.platformlayer.jobs.model.JobExecutionList;
import org.platformlayer.jobs.model.JobLog;
import org.platformlayer.metrics.model.MetricDataStream;
import org.platformlayer.metrics.model.MetricInfoCollection;
import org.platformlayer.metrics.model.MetricQuery;
import org.platformlayer.ops.OpsException;

public interface PlatformLayerClient {
	// Actions
	public JobData doAction(PlatformLayerKey key, Action action) throws PlatformLayerClientException;

	// Item CRUD
	// public <T> Iterable<T> listItems(Class<T> clazz) throws PlatformLayerClientException;
	//
	// public <T> Iterable<T> listItems(Class<T> clazz, Filter filter) throws PlatformLayerClientException;

	// public <T> T getItem(Class<T> clazz, PlatformLayerKey key) throws PlatformLayerClientException;

	// @Deprecated
	// // Not idempotent - use putItem
	// public <T> T createItem(T item) throws PlatformLayerClientException;
	//
	// @Deprecated
	// // Not idempotent - use putItem
	// public String createItem(ServiceType serviceType, ItemType itemType, String data, Format format) throws
	// PlatformLayerClientException;

	public UntypedItem putItem(PlatformLayerKey key, String data, Format dataFormat)
			throws PlatformLayerClientException;

	public <T extends ItemBase> T putItem(T item) throws OpsException;

	public UntypedItem putItemByTag(PlatformLayerKey key, Tag uniqueTag, String data, Format dataFormat)
			throws PlatformLayerClientException;

	public <T extends ItemBase> T putItemByTag(T item, Tag uniqueTag) throws OpsException;

	// public <T> T putItem(T item) throws PlatformLayerClientException;

	public JobData deleteItem(PlatformLayerKey key) throws PlatformLayerClientException;

	// Item Crud - Untyped
	public UntypedItem getItemUntyped(PlatformLayerKey key) throws PlatformLayerClientException;

	public UntypedItemCollection listItemsUntyped(PlatformLayerKey path) throws PlatformLayerClientException;

	public <T> List<T> listItems(Class<T> clazz) throws PlatformLayerClientException, OpsException;

	public UntypedItemCollection listRoots() throws PlatformLayerClientException;

	public UntypedItemCollection listChildren(PlatformLayerKey parent) throws PlatformLayerClientException;

	public List<ItemBase> listChildrenTyped(PlatformLayerKey parent) throws OpsException;

	// Tags
	public Tags changeTags(PlatformLayerKey key, TagChanges tagChanges, Long ifVersion)
			throws PlatformLayerClientException;

	// Jobs
	public JobDataList listJobs() throws PlatformLayerClientException;

	public JobLog getJobExecutionLog(String jobId, String executionId) throws PlatformLayerClientException;

	// Metrics
	public MetricDataStream getMetric(MetricQuery query) throws PlatformLayerClientException;

	public MetricInfoCollection listMetrics(PlatformLayerKey key) throws PlatformLayerClientException;

	// Service info
	public Collection<ServiceInfo> listServices(boolean allowCache) throws PlatformLayerClientException;

	@Deprecated
	public String activateService(String serviceType, String data, Format format) throws PlatformLayerClientException;

	@Deprecated
	public String getActivation(String serviceType, Format format) throws PlatformLayerClientException;

	// Misc
	public String getSshPublicKey(String serviceType) throws PlatformLayerClientException;

	public String getSchema(String serviceType, Format format) throws PlatformLayerClientException;

	public void ensureLoggedIn() throws PlatformLayerAuthenticationException;

	public ProjectId getProject();

	public PlatformLayerEndpointInfo getEndpointInfo(PlatformLayerKey item);

	public JobExecutionList listJobExecutions(String jobId) throws PlatformLayerClientException;

	public <T> T findItem(PlatformLayerKey key, Class<T> itemClass) throws OpsException;

	public <T> T findItem(PlatformLayerKey key) throws OpsException;

}
