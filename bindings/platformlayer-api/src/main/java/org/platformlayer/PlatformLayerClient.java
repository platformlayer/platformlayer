package org.platformlayer;

import java.util.Collection;

import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.ServiceInfo;
import org.platformlayer.core.model.Tag;
import org.platformlayer.core.model.TagChanges;
import org.platformlayer.core.model.Tags;
import org.platformlayer.ids.ProjectId;
import org.platformlayer.jobs.model.JobData;
import org.platformlayer.jobs.model.JobLog;
import org.platformlayer.metrics.model.MetricInfoCollection;
import org.platformlayer.metrics.model.MetricValues;

public interface PlatformLayerClient {
	// Actions
	public JobData doAction(PlatformLayerKey key, String action) throws PlatformLayerClientException;

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

	public UntypedItem putItemByTag(PlatformLayerKey key, Tag uniqueTag, String data, Format dataFormat)
			throws PlatformLayerClientException;

	// public <T> T putItem(T item) throws PlatformLayerClientException;

	public JobData deleteItem(PlatformLayerKey key) throws PlatformLayerClientException;

	// Item Crud - Untyped
	public UntypedItem getItemUntyped(PlatformLayerKey key) throws PlatformLayerClientException;

	public Iterable<UntypedItem> listItemsUntyped(PlatformLayerKey path) throws PlatformLayerClientException;

	public Iterable<UntypedItem> listRoots() throws PlatformLayerClientException;

	public Iterable<UntypedItem> listChildren(PlatformLayerKey parent) throws PlatformLayerClientException;

	// Tags
	public Tags changeTags(PlatformLayerKey key, TagChanges tagChanges) throws PlatformLayerClientException;

	// Jobs
	public Iterable<JobData> listJobs() throws PlatformLayerClientException;

	public JobLog getJobLog(String jobId) throws PlatformLayerClientException;

	// Metrics
	public MetricValues getMetric(PlatformLayerKey key, String metricKey) throws PlatformLayerClientException;

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

}
