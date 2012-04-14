package org.platformlayer.federation;

import java.util.List;
import java.util.Map;

import org.platformlayer.DirectPlatformLayerClient;
import org.platformlayer.PlatformLayerClient;
import org.platformlayer.TypedItemMapper;
import org.platformlayer.TypedPlatformLayerClient;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.federation.model.FederationConfiguration;
import org.platformlayer.federation.model.FederationRule;
import org.platformlayer.federation.model.PlatformLayerConnectionConfiguration;
import org.platformlayer.ids.FederationKey;
import org.platformlayer.ids.ProjectId;
import org.platformlayer.ids.ServiceType;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Maps platform layer keys to one or more children.
 */
public class FederationMap {

	// FederationKey privateCloud = new FederationKey("private");
	// FederationKey publicCloud = new FederationKey("public");
	private final FederationConfiguration config;
	private final Map<FederationMapping, PlatformLayerConnectionConfiguration> allKeys;
	private TypedPlatformLayerClient localClient;
	private final TypedItemMapper mapper;

	FederationMap(TypedItemMapper mapper, FederationConfiguration config) {
		this.mapper = mapper;
		this.config = config;

		this.allKeys = collectSystems();
	}

	// private Collection<FederationKey> collectFederationKeys() {
	// Set<FederationKey> keys = Sets.newHashSet();
	//
	// for (FederationRule rule : config.rules) {
	// FederationKey key = toKey(rule);
	// keys.add(key);
	// }
	//
	// return keys;
	// }

	private Map<FederationMapping, PlatformLayerConnectionConfiguration> collectSystems() {
		Map<FederationMapping, PlatformLayerConnectionConfiguration> keys = Maps.newHashMap();

		for (PlatformLayerConnectionConfiguration child : config.systems) {
			FederationKey host = new FederationKey(child.server);
			ProjectId project = new ProjectId(child.tenant);
			FederationMapping key = new FederationMapping(host, project);
			if (keys.containsKey(key)) {
				throw new IllegalArgumentException("Duplicate key: " + key);
			}

			keys.put(key, child);
		}

		return keys;
	}

	private FederationMapping toKey(PlatformLayerKey original, FederationRule rule) {
		FederationKey targetHost = original.getHost();
		ProjectId targetProject = original.getProject();

		if (rule.target != null) {
			PlatformLayerConnectionConfiguration found = null;
			for (PlatformLayerConnectionConfiguration system : this.config.systems) {
				if (Objects.equal(system.key, rule.target)) {
					found = system;
				}
			}

			if (found == null) {
				throw new IllegalStateException("Cannot find target: " + rule.target);
			}

			targetHost = new FederationKey(found.server);
			targetProject = new ProjectId(found.tenant);
		}

		return new FederationMapping(targetHost, targetProject);
	}

	private FederationMapping buildLocal(PlatformLayerKey original) {
		FederationKey targetHost = original.getHost();
		if (targetHost == null) {
			targetHost = FederationKey.LOCAL_FEDERATION_KEY;
		}

		ProjectId targetProject = original.getProject();

		return new FederationMapping(targetHost, targetProject);
	}

	public Iterable<FederationMapping> getAllKeys() {
		return allKeys.keySet();
	}

	public TypedPlatformLayerClient buildClient(FederationMapping key) {
		PlatformLayerConnectionConfiguration config = allKeys.get(key);
		if (config == null) {
			throw new IllegalArgumentException("Unknown key: " + key);
		}

		PlatformLayerClient client = DirectPlatformLayerClient.buildUsingConfiguration(config);

		TypedPlatformLayerClient typedClient = new TypedPlatformLayerClient(client, mapper);
		return typedClient;
	}

	public List<FederationMapping> getClients(PlatformLayerKey path) {
		if (path.getHost() != null) {
			throw new IllegalStateException();
		}

		List<FederationMapping> keys = Lists.newArrayList();

		for (FederationRule rule : config.rules) {
			if (isMatch(rule, path)) {
				keys.add(toKey(path, rule));
			}
		}

		keys.add(buildLocal(path));

		return keys;

		// return keys;
		//
		//
		// ServiceType serviceType = path.getServiceType();
		// if (serviceType.getKey().equals("dns")) {
		// return Arrays.asList(publicCloud);
		// }
		//
		// return Arrays.asList(privateCloud);
	}

	public FederationMapping getClientForCreate(PlatformLayerKey path) {
		if (path.getHost() != null) {
			throw new IllegalStateException();
		}

		List<FederationMapping> keys = Lists.newArrayList();

		for (FederationRule rule : config.rules) {
			if (isMatch(rule, path)) {
				keys.add(toKey(path, rule));
			}
		}

		if (keys.isEmpty()) {
			keys.add(buildLocal(path));
		}

		if (keys.size() == 0) {
			return null;
		}

		if (keys.size() != 1) {
			throw new IllegalArgumentException();
		}

		return keys.get(0);
	}

	private boolean isMatch(FederationRule rule, PlatformLayerKey path) {
		ServiceType serviceType = path.getServiceType();
		if (rule.serviceType != null) {
			if (!Objects.equal(rule.serviceType, serviceType.getKey())) {
				return false;
			}
		}
		return true;
	}

	public void setLocalClient(TypedPlatformLayerClient localClient) {
		if (this.localClient != null) {
			throw new IllegalStateException();
		}

		this.localClient = localClient;
	}

	public TypedPlatformLayerClient getLocalClient() {
		return localClient;
	}

	// public FederationKey mapToClient(PlatformLayerKey key) {
	// if (key.getHost() != null) {
	// throw new IllegalStateException();
	// }
	//
	// ServiceType serviceType = key.getServiceType();
	//
	// for (FederationRule rule : config.rules) {
	// if (isMatch(rule, key)) {
	// return toKey(rule);
	// }
	// }
	//
	// // We rely on a default rlue
	// throw new IllegalStateException();
	// // if (serviceType.getKey().equals("dns")) {
	// // return publicCloud;
	// // }
	// //
	// // return privateCloud;
	// }

	// public static FederationMap buildUsingProperties(Properties properties) {
	// FederationMap map = new FederationMap(properties);
	//
	// return map;
	// }
}
