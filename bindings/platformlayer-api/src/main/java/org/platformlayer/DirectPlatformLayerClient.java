package org.platformlayer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Properties;

import org.openstack.keystone.auth.client.KeystoneAuthenticator;
import org.openstack.utils.PropertyUtils;
import org.platformlayer.auth.Authenticator;
import org.platformlayer.core.model.Action;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.ServiceInfo;
import org.platformlayer.core.model.ServiceInfoCollection;
import org.platformlayer.core.model.Tag;
import org.platformlayer.core.model.TagChanges;
import org.platformlayer.core.model.Tags;
import org.platformlayer.federation.model.PlatformLayerConnectionConfiguration;
import org.platformlayer.ids.ItemType;
import org.platformlayer.ids.ManagedItemId;
import org.platformlayer.ids.ProjectId;
import org.platformlayer.ids.ServiceType;
import org.platformlayer.jobs.model.JobData;
import org.platformlayer.jobs.model.JobDataList;
import org.platformlayer.jobs.model.JobLog;
import org.platformlayer.metrics.model.MetricInfoCollection;
import org.platformlayer.metrics.model.MetricValues;
import org.platformlayer.xml.JaxbHelper;
import org.platformlayer.xml.UnmarshalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectPlatformLayerClient extends PlatformLayerClientBase {
    public static final String SERVICE_PLATFORMLAYER = "platformlayer";

    static final Logger log = LoggerFactory.getLogger(DirectPlatformLayerClient.class);

    List<ServiceInfo> services;

    private final ProjectId projectId;

    private final PlatformLayerHttpClient httpClient;

    private DirectPlatformLayerClient(PlatformLayerHttpClient httpClient, ProjectId projectId) {
        this.httpClient = httpClient;
        this.projectId = projectId;
    }

    public static DirectPlatformLayerClient buildUsingSavedConfiguration(String key) throws IOException {
        File credentialsFile = new File(System.getProperty("user.home") + File.separator + ".credentials" + File.separator + key);
        if (!credentialsFile.exists())
            throw new FileNotFoundException("Credentials file not found: " + credentialsFile);

        Properties properties;
        try {
            properties = PropertyUtils.loadProperties(credentialsFile);
        } catch (IOException e) {
            throw new IOException("Error reading credentials file: " + credentialsFile, e);
        }

        return buildUsingProperties(properties);
    }

    public static DirectPlatformLayerClient buildUsingConfiguration(PlatformLayerConnectionConfiguration config) {
        String tenant = config.tenant;
        String server = config.server;
        String username = config.username;
        String secret = config.secret;

        Authenticator authenticator = new KeystoneAuthenticator(tenant, username, secret, server);
        ProjectId projectId = new ProjectId(tenant);
        return build(authenticator, projectId);
    }

    public static DirectPlatformLayerClient buildUsingProperties(Properties properties) {
        PlatformLayerConnectionConfiguration config = new PlatformLayerConnectionConfiguration();
        config.tenant = properties.getProperty("platformlayer.tenant");
        config.server = properties.getProperty("platformlayer.auth");
        config.username = properties.getProperty("platformlayer.username");
        config.secret = properties.getProperty("platformlayer.password");

        return buildUsingConfiguration(config);
    }

    public static DirectPlatformLayerClient build(Authenticator authenticator, ProjectId projectId) {
        return new DirectPlatformLayerClient(new PlatformLayerHttpClient(authenticator), projectId);
    }

    public List<ServiceInfo> listServices(boolean allowCache) throws PlatformLayerClientException {
        if (!allowCache || services == null) {
            ServiceInfoCollection ret = httpClient.doSimpleRequest("/", ServiceInfoCollection.class, Format.XML);
            services = ret.services;
        }

        return services;
    }

    public void ensureLoggedIn() throws PlatformLayerAuthenticationException {
        httpClient.getAuthenticationToken();
    }

    // public String createItem(ServiceType serviceType, ItemType itemType, String data, Format format) throws
    // PlatformLayerClientException {
    // String relativePath = buildRelativePath(serviceType, itemType, null);
    //
    // return httpClient.doRequest("POST", relativePath, String.class, data, format);
    // }

    public UntypedItem putItem(ServiceType serviceType, ItemType itemType, ManagedItemId id, String data, Format dataFormat) throws PlatformLayerClientException {
        if (id == null) {
            throw new IllegalArgumentException("id is required on a put");
        }

        String relativePath = buildRelativePath(serviceType, itemType, id);

        String xml = httpClient.doRequest("PUT", relativePath, String.class, Format.XML, data, dataFormat);
        return UntypedItem.build(xml);
    }

    @Override
    public UntypedItem putItemByTag(PlatformLayerKey key, Tag uniqueTag, String data, Format dataFormat) throws PlatformLayerClientException {
        if (key.getItemId() == null) {
            throw new IllegalArgumentException("id is required on a put");
        }

        String relativePath = buildRelativePath(key);

        if (uniqueTag != null) {
            relativePath += "?unique=" + encodeArgument(uniqueTag.getKey());
        }

        String xml = httpClient.doRequest("PUT", relativePath, String.class, Format.XML, data, dataFormat);
        UntypedItem item = UntypedItem.build(xml);

        // PlatformLayerKey platformLayerKey = item.getPlatformLayerKey();
        // platformLayerKey = platformLayerKey.withId(new ManagedItemId(item.getId()));
        // item.setPlatformLayerKey(platformLayerKey);

        return item;
    }

    private String encodeArgument(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 not supported", e);
        }
    }

    public UntypedItem putItem(PlatformLayerKey key, String data, Format dataFormat) throws PlatformLayerClientException {
        return putItem(key.getServiceType(), key.getItemType(), key.getItemId(), data, dataFormat);
    }

    public String activateService(String serviceType, String data, Format dataFormat) throws PlatformLayerClientException {
        String relativePath = "/authorizations/" + serviceType + "/";

        return httpClient.doRequest("POST", relativePath, String.class, Format.XML, data, dataFormat);
    }

    public String getActivation(String serviceType, Format format) throws PlatformLayerClientException {
        String relativePath = "/authorizations/" + serviceType + "/";

        return httpClient.doRequest("GET", relativePath, String.class, format, null, null);
    }

    public String getSshPublicKey(String serviceType) throws PlatformLayerClientException {
        String relativePath = "/" + serviceType + "/sshkey";

        return httpClient.doRequest("GET", relativePath, String.class, Format.TEXT, null, null);
    }

    public String getSchema(String serviceType, Format format) throws PlatformLayerClientException {
        String relativePath = "/" + serviceType + "/schema";

        return httpClient.doRequest("GET", relativePath, String.class, format, null, null);
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

    public Tags changeTags(PlatformLayerKey key, TagChanges tagChanges) throws PlatformLayerClientException {
        return changeTags(key.getServiceType(), key.getItemType(), key.getItemId(), tagChanges);
    }

    public Tags changeTags(ServiceType serviceType, ItemType itemType, ManagedItemId id, TagChanges tagChanges) throws PlatformLayerClientException {
        String url = buildRelativePath(serviceType, itemType, id) + "/tags";
        Tags retval = httpClient.doRequest("POST", url, Tags.class, Format.XML, tagChanges, Format.XML);
        return retval;
    }

    // public <T> void deleteItem(T item) throws PlatformLayerClientException {
    // JaxbHelper jaxbHelper = toJaxbHelper(item);
    //
    // PlatformLayerKey key = toKey(jaxbHelper, item);
    // deleteItem(key);
    // }

    public void deleteItem(PlatformLayerKey key) throws PlatformLayerClientException {
        String relativePath = buildRelativePath(key);

        httpClient.doRequest("DELETE", relativePath, String.class, Format.XML, null, null);
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
        String retval = httpClient.doRequest("GET", relativePath, String.class, Format.XML, null, null);
        return retval;
    }

    public UntypedItemCollection listItemsUntyped(PlatformLayerKey path) throws PlatformLayerClientException {
        String xml = doListItemsRequest(path);

        return UntypedItemCollection.build(xml);
    }

    private String doListRootsRequest() throws PlatformLayerClientException {
        String relativePath = "roots";
        String retval = httpClient.doRequest("GET", relativePath, String.class, Format.XML, null, null);
        return retval;
    }

    public UntypedItemCollection listRoots() throws PlatformLayerClientException {
        String xml = doListRootsRequest();

        return UntypedItemCollection.build(xml);
    }

    public UntypedItemCollection listChildren(PlatformLayerKey parent) throws PlatformLayerClientException {
        String relativePath = buildRelativePath(parent) + "/children";
        String xml = httpClient.doRequest("GET", relativePath, String.class, Format.XML, null, null);

        return UntypedItemCollection.build(xml);
    }

    public UntypedItem getItemUntyped(PlatformLayerKey key) throws PlatformLayerClientException {
        String relativePath = buildRelativePath(key);

        String xml = httpClient.doRequest("GET", relativePath, String.class, Format.XML, null, null);

        UntypedItem item = UntypedItem.build(xml);

        return item;
    }

    public JobDataList listJobs() throws PlatformLayerClientException {
        String relativePath = "/jobs";
        JobDataList jobs = httpClient.doRequest("GET", relativePath, JobDataList.class, Format.XML, null, null);

        return jobs;
    }

    public JobLog getJobLog(String jobId) throws PlatformLayerClientException {
        String relativePath = "/jobs/" + jobId + "/log";
        JobLog log = httpClient.doRequest("GET", relativePath, JobLog.class, Format.XML, null, null);

        return log;
    }

    public MetricInfoCollection listMetrics(PlatformLayerKey key) throws PlatformLayerClientException {
        String relativePath = buildRelativePath(key) + "/metrics";

        String retval = httpClient.doRequest("GET", relativePath, String.class, Format.XML, null, null);
        MetricInfoCollection items;
        try {
            items = JaxbHelper.deserializeXmlObject(retval, MetricInfoCollection.class);
        } catch (UnmarshalException e) {
            throw new PlatformLayerClientException("Error parsing returned data", e);
        }
        return items;
    }

    public MetricValues getMetric(PlatformLayerKey key, String metricKey) throws PlatformLayerClientException {
        String relativePath = buildRelativePath(key) + "/metrics/" + metricKey;

        String retval = httpClient.doRequest("GET", relativePath, String.class, Format.XML, null, null);
        MetricValues items;
        try {
            items = JaxbHelper.deserializeXmlObject(retval, MetricValues.class);
        } catch (UnmarshalException e) {
            throw new PlatformLayerClientException("Error parsing returned data", e);
        }
        return items;
    }

    public JobData doAction(PlatformLayerKey key, String action) throws PlatformLayerClientException {
        String relativePath = buildRelativePath(key) + "/actions";

        if (action == null || action.isEmpty()) {
            throw new IllegalArgumentException();
        }

        Action actionCommand = new Action(action);

        JobData retval = httpClient.doRequest("POST", relativePath, JobData.class, Format.XML, actionCommand, Format.XML);
        return retval;
    }

    @Override
    public ProjectId getProject() {
        return projectId;
    }

    // protected String buildRelativePath(ServiceType serviceType, ItemType itemType) {
    // return urlEncode(serviceType.getKey()) + "/" + urlEncode(itemType.getKey()) + "/";
    // }

}
