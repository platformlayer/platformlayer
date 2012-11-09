package org.platformlayer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Properties;

import org.openstack.utils.PropertyUtils;
import org.platformlayer.auth.Authenticator;
import org.platformlayer.auth.client.PlatformlayerAuthenticator;
import org.platformlayer.common.UntypedItem;
import org.platformlayer.common.UntypedItemCollection;
import org.platformlayer.core.model.Action;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.ServiceInfo;
import org.platformlayer.core.model.ServiceInfoCollection;
import org.platformlayer.core.model.Tag;
import org.platformlayer.core.model.TagChanges;
import org.platformlayer.core.model.Tags;
import org.platformlayer.federation.model.PlatformLayerConnectionConfiguration;
import org.platformlayer.http.HttpStrategy;
import org.platformlayer.ids.ItemType;
import org.platformlayer.ids.ManagedItemId;
import org.platformlayer.ids.ProjectId;
import org.platformlayer.ids.ServiceType;
import org.platformlayer.jobs.model.JobData;
import org.platformlayer.jobs.model.JobDataList;
import org.platformlayer.jobs.model.JobExecutionData;
import org.platformlayer.jobs.model.JobExecutionList;
import org.platformlayer.jobs.model.JobLog;
import org.platformlayer.metrics.model.JsonMetricDataStream;
import org.platformlayer.metrics.model.MetricDataStream;
import org.platformlayer.metrics.model.MetricInfoCollection;
import org.platformlayer.metrics.model.MetricQuery;
import org.platformlayer.xml.JaxbHelper;
import org.platformlayer.xml.UnmarshalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class HttpPlatformLayerClient extends PlatformLayerClientBase {
	public static final String SERVICE_PLATFORMLAYER = "platformlayer";

	static final Logger log = LoggerFactory.getLogger(HttpPlatformLayerClient.class);

	List<ServiceInfo> services;

	private final ProjectId projectId;

	private final PlatformLayerHttpTransport httpClient;

	private HttpPlatformLayerClient(PlatformLayerHttpTransport httpClient, ProjectId projectId) {
		this.httpClient = httpClient;
		this.projectId = projectId;
	}

	public static HttpPlatformLayerClient buildUsingSavedConfiguration(HttpStrategy httpStrategy, String key)
			throws IOException {
		File credentialsFile = new File(System.getProperty("user.home") + File.separator + ".credentials"
				+ File.separator + key);
		if (!credentialsFile.exists()) {
			throw new FileNotFoundException("Credentials file not found: " + credentialsFile);
		}

		Properties properties;
		try {
			properties = PropertyUtils.loadProperties(credentialsFile);
		} catch (IOException e) {
			throw new IOException("Error reading credentials file: " + credentialsFile, e);
		}

		return buildUsingProperties(httpStrategy, properties);
	}

	public static HttpPlatformLayerClient buildUsingConfiguration(HttpStrategy httpStrategy,
			PlatformLayerConnectionConfiguration config) {
		String project = config.tenant;
		String server = config.authenticationEndpoint;
		String username = config.username;
		String secret = config.secret;
		List<String> authTrustKeys = config.authTrustKeys;

		Authenticator authenticator = new PlatformlayerAuthenticator(httpStrategy, username, secret, server,
				authTrustKeys);
		ProjectId projectId = new ProjectId(project);

		return build(httpStrategy, config.platformlayerEndpoint, authenticator, projectId,
				config.platformlayerTrustKeys);
	}

	public static HttpPlatformLayerClient buildUsingProperties(HttpStrategy httpStrategy, Properties properties) {
		PlatformLayerConnectionConfiguration config = new PlatformLayerConnectionConfiguration();
		config.tenant = properties.getProperty("platformlayer.tenant");
		config.authenticationEndpoint = properties.getProperty("platformlayer.auth.url");
		config.username = properties.getProperty("platformlayer.username");
		config.secret = properties.getProperty("platformlayer.password");
		config.platformlayerEndpoint = properties.getProperty("platformlayer.url");

		String trustKeys = properties.getProperty("platformlayer.ssl.keys", null);
		if (!Strings.isNullOrEmpty(trustKeys)) {
			config.platformlayerTrustKeys = Lists.newArrayList(Splitter.on(',').trimResults().split(trustKeys));
		}

		String authTrustKeys = properties.getProperty("platformlayer.auth.ssl.keys", null);
		if (!Strings.isNullOrEmpty(authTrustKeys)) {
			config.authTrustKeys = Lists.newArrayList(Splitter.on(',').trimResults().split(authTrustKeys));
		}

		return buildUsingConfiguration(httpStrategy, config);
	}

	public static HttpPlatformLayerClient build(HttpStrategy httpStrategy, String platformlayerBaseUrl,
			Authenticator authenticator, ProjectId projectId, List<String> trustKeys) {
		String url = platformlayerBaseUrl;
		if (!url.endsWith("/")) {
			url += "/";
		}

		return new HttpPlatformLayerClient(new PlatformLayerHttpTransport(httpStrategy, url, authenticator, trustKeys),
				projectId);
	}

	@Override
	public List<ServiceInfo> listServices(boolean allowCache) throws PlatformLayerClientException {
		if (!allowCache || services == null) {
			ServiceInfoCollection ret = doRequest("GET", "", ServiceInfoCollection.class, Format.XML, null, null);
			services = ret.services;
		}

		return services;
	}

	@Override
	public void ensureLoggedIn() throws PlatformLayerAuthenticationException {
		httpClient.getAuthenticationToken();
	}

	// public String createItem(ServiceType serviceType, ItemType itemType, String data, Format format) throws
	// PlatformLayerClientException {
	// String relativePath = buildRelativePath(serviceType, itemType, null);
	//
	// return httpClient.doRequest("POST", relativePath, String.class, data, format);
	// }

	private <T> T doRequest(String method, String relativeUrl, Class<T> retvalClass, Format acceptFormat,
			Object sendData, Format sendDataFormat) throws PlatformLayerClientException {
		if (relativeUrl.startsWith("/")) {
			assert false;
			relativeUrl = relativeUrl.substring(1);
		}

		relativeUrl = projectId.getKey() + "/" + relativeUrl;

		return httpClient.doRequest(method, relativeUrl, retvalClass, acceptFormat, sendData, sendDataFormat);
	}

	public UntypedItem putItem(ServiceType serviceType, ItemType itemType, ManagedItemId id, String data,
			Format dataFormat) throws PlatformLayerClientException {
		if (id == null) {
			throw new IllegalArgumentException("id is required on a put");
		}

		String relativePath = buildRelativePath(serviceType, itemType, id);

		String xml = doRequest("PUT", relativePath, String.class, Format.XML, data, dataFormat);
		return UntypedItemXml.build(xml);
	}

	@Override
	public UntypedItem putItemByTag(PlatformLayerKey key, Tag uniqueTag, String data, Format dataFormat)
			throws PlatformLayerClientException {
		if (key.getItemId() == null) {
			throw new IllegalArgumentException("id is required on a put");
		}

		String relativePath = buildRelativePath(key);

		if (uniqueTag != null) {
			relativePath += "?unique=" + urlEncode(uniqueTag.getKey());
		}

		String xml = doRequest("PUT", relativePath, String.class, Format.XML, data, dataFormat);
		UntypedItem item = UntypedItemXml.build(xml);

		// PlatformLayerKey platformLayerKey = item.getPlatformLayerKey();
		// platformLayerKey = platformLayerKey.withId(new ManagedItemId(item.getId()));
		// item.setPlatformLayerKey(platformLayerKey);

		return item;
	}

	@Override
	public UntypedItem putItem(PlatformLayerKey key, String data, Format dataFormat)
			throws PlatformLayerClientException {
		return putItem(key.getServiceType(), key.getItemType(), key.getItemId(), data, dataFormat);
	}

	@Override
	public String activateService(String serviceType, String data, Format dataFormat)
			throws PlatformLayerClientException {
		String relativePath = "authorizations/" + serviceType + "/";

		return doRequest("POST", relativePath, String.class, Format.XML, data, dataFormat);
	}

	@Override
	public String getActivation(String serviceType, Format format) throws PlatformLayerClientException {
		String relativePath = "authorizations/" + serviceType + "/";

		return doRequest("GET", relativePath, String.class, format, null, null);
	}

	@Override
	public String getSshPublicKey(String serviceType) throws PlatformLayerClientException {
		String relativePath = serviceType + "/sshkey";

		return doRequest("GET", relativePath, String.class, Format.TEXT, null, null);
	}

	@Override
	public String getSchema(String serviceType, Format format) throws PlatformLayerClientException {
		String relativePath = serviceType + "/schema";

		return doRequest("GET", relativePath, String.class, format, null, null);
	}

	// public <T> T createItem(T item) throws PlatformLayerClientException {
	// JaxbHelper jaxbHelper = toJaxbHelper(item);
	//
	// String xml = serialize(jaxbHelper, item);
	//
	// PlatformLayerKey key = toKey(jaxbHelper, item);
	//
	// String relativePath = buildRelativePath(key);
	//
	// String retval = httpClient.doRequest("POST", relativePath, String.class, xml, Format.XML);
	//
	// T created = deserializeItem((Class<T>) item.getClass(), retval);
	//
	// setPlatformLayerKey(created, key);
	//
	// return created;
	// }

	@Override
	public Tags changeTags(PlatformLayerKey key, TagChanges tagChanges) throws PlatformLayerClientException {
		return changeTags(key.getServiceType(), key.getItemType(), key.getItemId(), tagChanges);
	}

	public Tags changeTags(ServiceType serviceType, ItemType itemType, ManagedItemId id, TagChanges tagChanges)
			throws PlatformLayerClientException {
		String url = buildRelativePath(serviceType, itemType, id) + "/tags";
		Tags retval = doRequest("POST", url, Tags.class, Format.XML, tagChanges, Format.XML);
		return retval;
	}

	// public <T> void deleteItem(T item) throws PlatformLayerClientException {
	// JaxbHelper jaxbHelper = toJaxbHelper(item);
	//
	// PlatformLayerKey key = toKey(jaxbHelper, item);
	// deleteItem(key);
	// }

	@Override
	public JobExecutionData deleteItem(PlatformLayerKey key) throws PlatformLayerClientException {
		String relativePath = buildRelativePath(key);

		JobExecutionData retval = doRequest("DELETE", relativePath, JobExecutionData.class, Format.XML, null, null);
		return retval;
	}

	// public <T> List<T> listItems(Class<T> clazz) throws PlatformLayerClientException {
	// return listItems(clazz, (Filter) null);
	// }
	//
	// public <T> List<T> listItems(Class<T> clazz, Tag tag) throws PlatformLayerClientException {
	// return listItems(clazz, Filter.byTag(tag));
	// }

	// public <T> List<T> listItems(Class<T> clazz, Filter filter) throws PlatformLayerClientException {
	// JaxbHelper jaxbHelper = toJaxbHelper(clazz, ManagedItemCollection.class);
	// PlatformLayerKey key = toKey(jaxbHelper);
	//
	// String xml = doListItemsRequest(key);
	//
	// ManagedItemCollection<T> items;
	// try {
	// items = jaxbHelper.deserialize(new StringReader(xml), ManagedItemCollection.class);
	// } catch (UnmarshalException e) {
	// throw new PlatformLayerClientException("Error parsing returned data", e);
	// }
	//
	// if (filter != null) {
	// // TODO: Do filtering server-side
	// List<T> filtered = Lists.newArrayList();
	// for (T item : items.items) {
	// if (filter.matches(item)) {
	// filtered.add(item);
	// }
	// }
	// return filtered;
	// } else {
	// return items.items;
	// }
	// }

	// public <T> T getItem(Class<T> clazz, PlatformLayerKey key) throws PlatformLayerClientException {
	// if (key.getHost() != null)
	// throw new UnsupportedOperationException();
	//
	// String relativePath = buildRelativePath(key);
	// T item = httpClient.doRequest("GET", relativePath, clazz, Format.XML, null, Format.XML);
	//
	// setPlatformLayerKey(item, key);
	//
	// return item;
	// }

	// private <T> void setPlatformLayerKey(T item, PlatformLayerKey key) {
	// if (item instanceof ItemBase) {
	// ((ItemBase) item).setKey(key);
	// } else {
	// throw new IllegalStateException();
	// }
	// }

	private String doListItemsRequest(PlatformLayerKey path) throws PlatformLayerClientException {
		String relativePath = buildRelativePath(path);
		String retval = doRequest("GET", relativePath, String.class, Format.XML, null, null);
		return retval;
	}

	@Override
	public UntypedItemCollection listItemsUntyped(PlatformLayerKey path) throws PlatformLayerClientException {
		String xml = doListItemsRequest(path);

		return UntypedItemXmlCollection.build(xml);
	}

	private String doListRootsRequest() throws PlatformLayerClientException {
		String relativePath = "roots";
		String retval = doRequest("GET", relativePath, String.class, Format.XML, null, null);
		return retval;
	}

	@Override
	public UntypedItemCollection listRoots() throws PlatformLayerClientException {
		String xml = doListRootsRequest();

		return UntypedItemXmlCollection.build(xml);
	}

	@Override
	public UntypedItemCollection listChildren(PlatformLayerKey parent) throws PlatformLayerClientException {
		String relativePath = buildRelativePath(parent) + "/children";
		String xml = doRequest("GET", relativePath, String.class, Format.XML, null, null);

		return UntypedItemXmlCollection.build(xml);
	}

	@Override
	public UntypedItem getItemUntyped(PlatformLayerKey key) throws PlatformLayerClientException {
		String relativePath = buildRelativePath(key);

		String xml = doRequest("GET", relativePath, String.class, Format.XML, null, null);

		UntypedItem item = UntypedItemXml.build(xml);

		return item;
	}

	@Override
	public JobDataList listJobs() throws PlatformLayerClientException {
		String relativePath = "jobs";
		JobDataList jobs = doRequest("GET", relativePath, JobDataList.class, Format.XML, null, null);

		return jobs;
	}

	@Override
	public MetricInfoCollection listMetrics(PlatformLayerKey key) throws PlatformLayerClientException {
		String relativePath = buildRelativePath(key) + "/metrics";

		String retval = doRequest("GET", relativePath, String.class, Format.XML, null, null);
		MetricInfoCollection items;
		try {
			items = JaxbHelper.deserializeXmlObject(retval, MetricInfoCollection.class);
		} catch (UnmarshalException e) {
			throw new PlatformLayerClientException("Error parsing returned data", e);
		}
		return items;
	}

	@Override
	public MetricDataStream getMetric(MetricQuery query) throws PlatformLayerClientException {
		String relativePath = buildRelativePath(query.item) + "/metrics";

		StreamingResponse response = doRequest("POST", relativePath, StreamingResponse.class, Format.JSON, query,
				Format.XML);
		MetricDataStream dataStream;
		try {
			dataStream = JsonMetricDataStream.build(response.getResponseStream());
			response = null; // Don't close yet
		} catch (IOException e) {
			throw new PlatformLayerClientException("Error parsing returned data", e);
		} finally {
			IoUtils.safeClose(response);
		}
		return dataStream;
	}

	@Override
	public JobData doAction(PlatformLayerKey key, Action action) throws PlatformLayerClientException {
		String relativePath = buildRelativePath(key) + "/actions";

		JobData retval = doRequest("POST", relativePath, JobData.class, Format.XML, action, Format.XML);
		return retval;
	}

	@Override
	public ProjectId getProject() {
		return projectId;
	}

	public void setDebug(PrintStream debug) {
		httpClient.setDebug(debug);
	}

	@Override
	public PlatformLayerEndpointInfo getEndpointInfo(PlatformLayerKey item) {
		return httpClient.getEndpointInfo(projectId);
	}

	@Override
	public JobLog getJobExecutionLog(String jobId, String executionId) throws PlatformLayerClientException {
		String relativePath = "jobs/" + jobId + "/runs/" + executionId + "/log";
		JobLog jobLog = doRequest("GET", relativePath, JobLog.class, Format.XML, null, null);
		return jobLog;
	}

	@Override
	public JobExecutionList listJobExecutions(PlatformLayerKey jobKey) throws PlatformLayerClientException {
		String relativePath = "jobs/" + jobKey.getItemIdString() + "/runs";
		JobExecutionList executions = doRequest("GET", relativePath, JobExecutionList.class, Format.XML, null, null);
		return executions;
	}

	// protected String buildRelativePath(ServiceType serviceType, ItemType itemType) {
	// return urlEncode(serviceType.getKey()) + "/" + urlEncode(itemType.getKey()) + "/";
	// }

}
