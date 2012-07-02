package org.platformlayer.federation;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;
import org.platformlayer.CheckedFunction;
import org.platformlayer.Format;
import org.platformlayer.PlatformLayerAuthenticationException;
import org.platformlayer.PlatformLayerClient;
import org.platformlayer.PlatformLayerClientBase;
import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.PlatformLayerClientNotFoundException;
import org.platformlayer.TypedItemMapper;
import org.platformlayer.TypedPlatformLayerClient;
import org.platformlayer.UntypedItem;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.ServiceInfo;
import org.platformlayer.core.model.Tag;
import org.platformlayer.core.model.TagChanges;
import org.platformlayer.core.model.Tags;
import org.platformlayer.federation.model.FederationConfiguration;
import org.platformlayer.federation.model.FederationRule;
import org.platformlayer.federation.model.PlatformLayerConnectionConfiguration;
import org.platformlayer.forkjoin.FakeForkJoinStrategy;
import org.platformlayer.forkjoin.ForkJoinStrategy;
import org.platformlayer.forkjoin.ListConcatentation;
import org.platformlayer.ids.FederationKey;
import org.platformlayer.ids.ManagedItemId;
import org.platformlayer.ids.ProjectId;
import org.platformlayer.jobs.model.JobData;
import org.platformlayer.jobs.model.JobLog;
import org.platformlayer.metrics.model.MetricInfoCollection;
import org.platformlayer.metrics.model.MetricValues;
import org.platformlayer.ops.OpsException;
import org.platformlayer.service.federation.v1.FederatedService;
import org.platformlayer.service.federation.v1.FederatedServiceMap;
import org.platformlayer.xml.JaxbHelper;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class FederatedPlatformLayerClient extends PlatformLayerClientBase {
	static final Logger log = Logger.getLogger(FederatedPlatformLayerClient.class);

	// TODO: We could maybe do this with a Dynamic Proxy (i.e. MethodInvocation magic)??

	final ForkJoinStrategy forkJoinPool;

	final Map<FederationMapping, ChildClient> childClients = Maps.newHashMap();

	final FederationMap federationMap;

	final ProjectId defaultProject;

	public FederatedPlatformLayerClient(ProjectId defaultProject, FederationMap federationMap,
			ForkJoinStrategy forkJoinPool) {
		this.defaultProject = defaultProject;
		this.federationMap = federationMap;
		this.forkJoinPool = forkJoinPool;

		buildClients();
	}

	// public static FederatedPlatformLayerClient buildUsingSavedConfiguration(String key) throws IOException {
	// File credentialsFile = new File(System.getProperty("user.home") + File.separator + ".credentials" +
	// File.separator + key);
	// if (!credentialsFile.exists())
	// throw new FileNotFoundException("Configuration file not found: " + credentialsFile);
	// Properties properties = new Properties();
	// try {
	// properties.load(new FileInputStream(credentialsFile));
	// } catch (IOException e) {
	// throw new IOException("Error reading configuration file: " + credentialsFile, e);
	// }
	// return buildUsingProperties(properties);
	// }

	// public static FederatedPlatformLayerClient buildUsingConfig(InputStream is, TypedItemMapper mapper)
	// throws OpsException {
	// FederationConfiguration federationMapConfig = SmartDeserialization.deserialize(FederationConfiguration.class,
	// is);
	// FederationMap federationMap = new FederationMap(mapper, federationMapConfig);
	//
	// // int parallelism = Runtime.getRuntime().availableProcessors();
	// // // Because we're doing lots of HTTP requests, rather than being CPU bound, we massively increase the
	// // parallelism
	// // parallelism *= 256;
	//
	// ForkJoinStrategy forkJoinPool = new FakeForkJoinStrategy();
	//
	// return new FederatedPlatformLayerClient(federationMap, forkJoinPool);
	// }

	void buildClients() {
		// TODO: Fork/Join?

		for (FederationMapping key : federationMap.getAllTargetKeys()) {
			TypedPlatformLayerClient client = federationMap.buildClient(key);

			ChildClient child = new ChildClient(key, client);
			childClients.put(key, child);
		}

		if (childClients.isEmpty()) {
			throw new IllegalStateException();
		}
	}

	static class ChildClient {
		public final FederationMapping key;
		public final TypedPlatformLayerClient client;

		public ChildClient(FederationMapping key, TypedPlatformLayerClient client) {
			this.key = key;
			this.client = client;

			if (key == null) {
				throw new IllegalStateException();
			}
		}

		public UntypedItem setHost(UntypedItem item) {
			// if (!key.equals(FederationKey.LOCAL_FEDERATION_KEY)) {
			PlatformLayerKey plk = item.getPlatformLayerKey();
			item.setPlatformLayerKey(changeHost(plk));
			// }
			return item;
		}

		public JobData setHost(JobData item) {
			// if (!key.equals(FederationKey.LOCAL_FEDERATION_KEY)) {
			PlatformLayerKey plk = item.key;
			item.key = changeHost(plk);
			// }
			return item;
		}

		private PlatformLayerKey changeHost(PlatformLayerKey plk) {
			return new PlatformLayerKey(key.host, key.project, plk.getServiceType(), plk.getItemType(), plk.getItemId());
		}

		public <T> T setHost(T item) {
			// if (!key.equals(FederationKey.LOCAL_FEDERATION_KEY)) {
			if (item instanceof ItemBase) {
				ItemBase itemBase = (ItemBase) item;

				// if (!key.equals(FederationKey.LOCAL_FEDERATION_KEY)) {
				PlatformLayerKey plk = itemBase.getKey();
				if (plk == null) {
					throw new IllegalStateException();
				}
				itemBase.setKey(changeHost(plk));
				// }
			} else {
				throw new IllegalStateException();
			}
			// }

			return item;
		}

		@Override
		public String toString() {
			return "ChildClient [key=" + key + "]";
		}
	}

	static class MappedPlatformLayerKey {
		public ChildClient child;
		public PlatformLayerKey key;
	}

	// @Override
	// public <T> Iterable<T> listItems(Class<T> clazz) throws PlatformLayerClientException {
	// return listItems(clazz, null);
	// }
	//
	// @Override
	// public <T> Iterable<T> listItems(final Class<T> clazz, Filter filter) throws PlatformLayerClientException {
	// PlatformLayerKey key = toKey(clazz);
	// return doListConcatenation(getChildClients(key), AddHostTyped.wrap(new ListItemsTyped<T>(clazz, filter)));
	// }
	static abstract class HostFunction<V> implements CheckedFunction<ChildClient, V, PlatformLayerClientException> {
		@Override
		public abstract V apply(final ChildClient child) throws PlatformLayerClientException;
	}

	static class ListItemsUntyped extends HostFunction<Iterable<UntypedItem>> {
		final PlatformLayerKey path;

		public ListItemsUntyped(PlatformLayerKey path) {
			this.path = path;
		}

		@Override
		public Iterable<UntypedItem> apply(final ChildClient child) throws PlatformLayerClientException {
			return child.client.listItemsUntyped(path);
		}
	}

	static class ListChildren extends HostFunction<Iterable<UntypedItem>> {
		final PlatformLayerKey parent;

		public ListChildren(PlatformLayerKey parent) {
			this.parent = parent;
		}

		@Override
		public Iterable<UntypedItem> apply(final ChildClient child) throws PlatformLayerClientException {
			try {
				return child.client.listChildren(parent);
			} catch (PlatformLayerClientNotFoundException e) {
				log.warn("Ignoring not found from federated client on: " + e.getUrl());
				return Collections.emptyList();
			}
		}
	}

	// static class ListItemsTyped<T> extends HostFunction<Iterable<T>> {
	// final Class<T> clazz;
	// final Filter filter;
	//
	// public ListItemsTyped(Class<T> clazz, Filter filter) {
	// this.clazz = clazz;
	// this.filter = filter;
	// }
	//
	// public Iterable<T> apply(final ChildClient child) throws PlatformLayerClientException {
	// return child.client.listItems(clazz, filter);
	// }
	// }

	static class ListServices extends HostFunction<Iterable<ServiceInfo>> {
		final boolean allowCache;

		public ListServices(boolean allowCache) {
			this.allowCache = allowCache;
		}

		@Override
		public Iterable<ServiceInfo> apply(final ChildClient child) throws PlatformLayerClientException {
			return child.client.listServices(allowCache);
		}
	}

	static class ListRoots extends HostFunction<Iterable<UntypedItem>> {
		@Override
		public Iterable<UntypedItem> apply(final ChildClient child) throws PlatformLayerClientException {
			return child.client.listRoots();
		}
	}

	static class ListJobs extends HostFunction<Iterable<JobData>> {
		@Override
		public Iterable<JobData> apply(final ChildClient child) throws PlatformLayerClientException {
			return child.client.listJobs();
		}
	}

	static class AddHostUntyped extends HostFunction<Iterable<UntypedItem>> {
		final HostFunction<Iterable<UntypedItem>> inner;

		public AddHostUntyped(HostFunction<Iterable<UntypedItem>> inner) {
			this.inner = inner;
		}

		public static AddHostUntyped wrap(HostFunction<Iterable<UntypedItem>> inner) {
			return new AddHostUntyped(inner);
		}

		@Override
		public Iterable<UntypedItem> apply(final ChildClient child) throws PlatformLayerClientException {
			return Iterables.transform(inner.apply(child), new Function<UntypedItem, UntypedItem>() {
				@Override
				public UntypedItem apply(UntypedItem item) {
					child.setHost(item);
					return item;
				}
			});
		}
	}

	static class AddHostTyped<T> extends HostFunction<Iterable<T>> {
		final HostFunction<Iterable<T>> inner;

		public AddHostTyped(HostFunction<Iterable<T>> inner) {
			this.inner = inner;
		}

		public static <T> AddHostTyped<T> wrap(HostFunction<Iterable<T>> inner) {
			return new AddHostTyped<T>(inner);
		}

		@Override
		public Iterable<T> apply(final ChildClient child) throws PlatformLayerClientException {
			return Iterables.transform(inner.apply(child), new Function<T, T>() {
				@Override
				public T apply(T item) {
					child.setHost(item);
					return item;
				}
			});
		}
	}

	static class AddHostToJob extends HostFunction<Iterable<JobData>> {
		final HostFunction<Iterable<JobData>> inner;

		public AddHostToJob(HostFunction<Iterable<JobData>> inner) {
			this.inner = inner;
		}

		public static AddHostToJob wrap(HostFunction<Iterable<JobData>> inner) {
			return new AddHostToJob(inner);
		}

		@Override
		public Iterable<JobData> apply(final ChildClient child) throws PlatformLayerClientException {
			return Iterables.transform(inner.apply(child), new Function<JobData, JobData>() {
				@Override
				public JobData apply(JobData item) {
					child.setHost(item);
					return item;
				}
			});
		}
	}

	@Override
	public Iterable<UntypedItem> listItemsUntyped(final PlatformLayerKey path) throws PlatformLayerClientException {
		return doListConcatenation(getChildClients(path), AddHostUntyped.wrap(new ListItemsUntyped(path)));
	}

	private <V> Iterable<V> doListConcatenation(Iterable<ChildClient> childClients, HostFunction<Iterable<V>> function)
			throws PlatformLayerClientException {
		try {
			return ListConcatentation.joinLists(forkJoinPool, childClients, function);
		} catch (ExecutionException e) {
			throw new PlatformLayerClientException("Error while building item list", e);
		}
	}

	// @Override
	// public <T> T getItem(final Class<T> clazz, final PlatformLayerKey key) throws PlatformLayerClientException {
	// MappedPlatformLayerKey mapped = mapToChild(key);
	// T item = mapped.child.client.getItem(clazz, mapped.key);
	// return mapped.child.setHost(item);
	// }

	// @Override
	// public <T> T createItem(T item) throws PlatformLayerClientException {
	// PlatformLayerKey key = toKey(item);
	//
	// MappedPlatformLayerKey mapped = mapToChildForCreate(key);
	//
	// T created = mapped.child.client.createItem(item);
	// return mapped.child.setHost(created);
	// }
	//
	// @Override
	// public String createItem(ServiceType serviceType, ItemType itemType, String data, Format format) throws
	// PlatformLayerClientException {
	// throw new UnsupportedOperationException();
	//
	// // PlatformLayerKey key = new PlatformLayerKey(serviceType, itemType, null);
	// // MappedPlatformLayerKey mapped = mapToChild(key);
	// // String s = child.client.createItem(serviceType, itemType, data, format);
	// // return child.setHost(s);
	// }

	@Override
	public UntypedItem putItem(PlatformLayerKey key, String data, Format format) throws PlatformLayerClientException {
		MappedPlatformLayerKey mapped = mapToChildForPut(key);

		UntypedItem untypedItem = UntypedItem.build(data);
		untypedItem.setPlatformLayerKey(mapped.key);

		UntypedItem item = mapped.child.client.putItem(mapped.key, untypedItem.serialize(), format);

		return mapped.child.setHost(item);
	}

	@Override
	public JobData deleteItem(PlatformLayerKey key) throws PlatformLayerClientException {
		MappedPlatformLayerKey mapped = mapToChild(key);
		JobData jobData = mapped.child.client.deleteItem(key);
		return mapped.child.setHost(jobData);
	}

	@Override
	public UntypedItem getItemUntyped(PlatformLayerKey key) throws PlatformLayerClientException {
		MappedPlatformLayerKey mapped = mapToChild(key);
		UntypedItem item = mapped.child.client.getItemUntyped(key);
		return mapped.child.setHost(item);
	}

	@Override
	public Iterable<UntypedItem> listRoots() throws PlatformLayerClientException {
		return doListConcatenation(getChildClients(), AddHostUntyped.wrap(new ListRoots()));
	}

	@Override
	public Iterable<JobData> listJobs() throws PlatformLayerClientException {
		return doListConcatenation(getChildClients(), AddHostToJob.wrap(new ListJobs()));
	}

	@Override
	public JobData doAction(PlatformLayerKey key, String action) throws PlatformLayerClientException {
		MappedPlatformLayerKey mapped = mapToChild(key);
		JobData result = mapped.child.client.doAction(mapped.key, action);
		return mapped.child.setHost(result);
	}

	@Override
	public Tags changeTags(PlatformLayerKey key, TagChanges tagChanges) throws PlatformLayerClientException {
		MappedPlatformLayerKey mapped = mapToChild(key);
		return mapped.child.client.changeTags(mapped.key, tagChanges);
	}

	@Override
	public JobLog getJobLog(String jobId) throws PlatformLayerClientException {
		throw new UnsupportedOperationException();
	}

	@Override
	public MetricValues getMetric(PlatformLayerKey key, String metricKey) throws PlatformLayerClientException {
		MappedPlatformLayerKey mapped = mapToChild(key);
		return mapped.child.client.getMetric(mapped.key, metricKey);
	}

	@Override
	public MetricInfoCollection listMetrics(PlatformLayerKey key) throws PlatformLayerClientException {
		MappedPlatformLayerKey mapped = mapToChild(key);
		return mapped.child.client.listMetrics(mapped.key);
	}

	Collection<ServiceInfo> servicesCache;

	@Override
	public Collection<ServiceInfo> listServices(boolean allowCache) throws PlatformLayerClientException {
		Collection<ServiceInfo> returnValue = servicesCache;
		if (!allowCache || returnValue == null) {
			Map<String, ServiceInfo> services = Maps.newHashMap();

			// We have to duplicate the results
			// TODO: Do we need to be smarter about how we dedup?
			for (ServiceInfo service : doListConcatenation(getChildClients(), new ListServices(allowCache))) {
				services.put(service.namespace, service);
			}

			returnValue = services.values();
			servicesCache = Lists.newArrayList(returnValue);
		}
		return returnValue;
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
		// throw new UnsupportedOperationException();
	}

	@Override
	protected PlatformLayerKey toKey(JaxbHelper jaxbHelper) throws PlatformLayerClientException {
		PlatformLayerKey key = super.toKey(jaxbHelper);
		// TODO: Add host
		return key;
	}

	protected <T> PlatformLayerKey toKey(T item) throws PlatformLayerClientException {
		if (item instanceof ItemBase) {
			ItemBase itemBase = (ItemBase) item;
			return itemBase.getKey();
		} else {
			throw new UnsupportedOperationException();
		}
	}

	protected PlatformLayerKey toKey(Class<?> c) throws PlatformLayerClientException {
		JaxbHelper jaxbHelper = toJaxbHelper(c, new Class[0]);
		return toKey(jaxbHelper);
	}

	private MappedPlatformLayerKey mapToChildForCreate(PlatformLayerKey plk) {
		if (plk.getItemId() != null) {
			throw new IllegalArgumentException();
		}

		FederationMapping childKey = federationMap.getClientForCreate(plk);
		ManagedItemId childItemId;

		ChildClient childClient = getClient(childKey);
		childItemId = null;

		MappedPlatformLayerKey mapped = new MappedPlatformLayerKey();
		mapped.child = childClient;
		mapped.key = new PlatformLayerKey(childKey.host, childKey.project, plk.getServiceType(), plk.getItemType(),
				childItemId);
		return mapped;
	}

	private MappedPlatformLayerKey mapToChildForPut(PlatformLayerKey plk) {
		FederationMapping childKey = federationMap.getClientForCreate(plk);
		ManagedItemId childItemId = plk.getItemId();

		ChildClient childClient = getClient(childKey);

		MappedPlatformLayerKey mapped = new MappedPlatformLayerKey();
		mapped.child = childClient;
		mapped.key = new PlatformLayerKey(childKey.host, childKey.project, plk.getServiceType(), plk.getItemType(),
				childItemId);
		return mapped;
	}

	private MappedPlatformLayerKey mapToChild(PlatformLayerKey plk) {
		// if (plk.getHost() != null) {
		//
		// }

		ManagedItemId itemId = plk.getItemId();
		if (itemId == null || itemId.isEmpty()) {
			throw new IllegalArgumentException();
		}

		FederationKey host = plk.getHost();
		if (host == null) {
			host = FederationKey.LOCAL;
		}

		ProjectId project = plk.getProject();
		if (project == null) {
			project = defaultProject;
			// project = federationMap.getLocalClient().getProject();
		}

		ChildClient childClient = getClient(new FederationMapping(host, project));

		MappedPlatformLayerKey mapped = new MappedPlatformLayerKey();
		mapped.child = childClient;

		mapped.key = new PlatformLayerKey(host, project, plk.getServiceType(), plk.getItemType(), plk.getItemId());

		return mapped;

		// Iterable<ChildClient> childClients = getChildClients(plk);

		// ChildClient client = null;
		// for (ChildClient childClient : childClients) {
		// if (client == null) {
		// client = childClient;
		// } else {
		// throw new IllegalStateException("Multiple clients found");
		// }
		// }
		// return client;
	}

	private Iterable<ChildClient> getChildClients() {
		return childClients.values();
	}

	private Iterable<ChildClient> getChildClients(PlatformLayerKey path) {
		List<ChildClient> clients = Lists.newArrayList();

		for (FederationMapping key : federationMap.getClients(path)) {
			ChildClient child = getClient(key);
			clients.add(child);
		}

		return clients;
	}

	private ChildClient getClient(FederationMapping key) {
		if (key.project == null) {
			key = new FederationMapping(key.host, getProject());
		}

		if (key.host == null) {
			key = new FederationMapping(FederationKey.LOCAL, key.project);
		}

		ChildClient child = this.childClients.get(key);
		if (child == null) {
			throw new IllegalStateException();
		}
		return child;
	}

	private <T> T oneOrNull(Iterable<T> iterable) throws PlatformLayerClientException {
		T item = null;
		int count = 0;
		for (T i : iterable) {
			if (count == 0) {
				item = i;
			} else {
				throw new PlatformLayerClientException("Expected exactly one matching item");
			}
		}
		return item;
	}

	// public static PlatformLayerClient build(TypedPlatformLayerClient localClient, TypedItemMapper mapper)
	// throws OpsException {
	// FederationMap federationMap = buildFederationMap(localClient, mapper);
	//
	// ForkJoinStrategy forkJoinPool = new FakeForkJoinStrategy();
	//
	// return new FederatedPlatformLayerClient(federationMap, forkJoinPool);
	// }

	public static PlatformLayerClient build(ProjectId defaultProject, FederationMap federationMap) throws OpsException {
		ForkJoinStrategy forkJoinPool = new FakeForkJoinStrategy();

		return new FederatedPlatformLayerClient(defaultProject, federationMap, forkJoinPool);
	}

	public static FederationMap buildFederationMap(TypedPlatformLayerClient localClient, TypedItemMapper mapper)
			throws OpsException {
		FederationConfiguration federationMapConfig = buildFederationConfiguration(localClient);

		FederationMap federationMap = new FederationMap(mapper, federationMapConfig);

		if (localClient != null) {
			federationMap.addDefault(localClient);
		}

		return federationMap;
	}

	public static FederationConfiguration buildFederationConfiguration(TypedPlatformLayerClient localClient)
			throws OpsException {
		FederationConfiguration federationMapConfig = new FederationConfiguration();

		for (FederatedService service : localClient.listItems(FederatedService.class)) {
			PlatformLayerConnectionConfiguration config = new PlatformLayerConnectionConfiguration();
			config.key = service.getKey();
			config.secret = service.getSecret();
			config.server = service.getServer();
			config.tenant = service.getTenant();
			config.username = service.getUsername();
			config.platformlayerEndpoint = service.getServer();

			federationMapConfig.systems.add(config);
		}

		for (FederatedServiceMap map : localClient.listItems(FederatedServiceMap.class)) {
			FederationRule rule = new FederationRule();
			rule.target = map.getTarget();
			rule.serviceType = map.getServiceType();

			federationMapConfig.rules.add(rule);
		}
		return federationMapConfig;
	}

	@Override
	public UntypedItem putItemByTag(PlatformLayerKey key, Tag uniqueTag, String data, Format format)
			throws PlatformLayerClientException {
		MappedPlatformLayerKey mapped = mapToChildForPut(key);

		UntypedItem post = UntypedItem.build(data);
		post.setPlatformLayerKey(mapped.key);

		UntypedItem item = mapped.child.client.putItemByTag(mapped.key, uniqueTag, post.serialize(), format);
		return mapped.child.setHost(item);
	}

	@Override
	public Iterable<UntypedItem> listChildren(PlatformLayerKey parent) throws PlatformLayerClientException {
		return doListConcatenation(getChildClients(parent), AddHostUntyped.wrap(new ListChildren(parent)));
	}

	@Override
	public ProjectId getProject() {
		return defaultProject; // federationMap.getLocalClient().getProject();
	}

}
