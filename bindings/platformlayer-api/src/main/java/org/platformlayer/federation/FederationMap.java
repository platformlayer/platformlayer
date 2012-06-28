package org.platformlayer.federation;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
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

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Maps platform layer keys to one or more children.
 */
public class FederationMap {
	private static final Logger log = Logger.getLogger(FederationMap.class);

	private final Map<FederationMapping, MappedTarget> targetMap;
	private final TypedItemMapper mapper;

	final List<Rule> rules;

	static class MappedTarget {
		PlatformLayerConnectionConfiguration configuration;
		TypedPlatformLayerClient client;
	}

	public static class Rule {
		public FederationMapping targetKey;
		public PlatformLayerKey mappedItems;
	}

	public FederationMap(TypedItemMapper mapper, FederationConfiguration config) {
		this.mapper = mapper;

		this.rules = Lists.newArrayList();

		this.targetMap = Maps.newHashMap();

		buildTargetMap(config);
		buildRules(config);
	}

	public void addDefault(TypedPlatformLayerClient defaultClient) {
		FederationKey host = FederationKey.LOCAL;
		ProjectId project = defaultClient.getProject();
		FederationMapping mapKey = new FederationMapping(host, project);

		MappedTarget target = new MappedTarget();
		target.client = defaultClient;

		addMapping(mapKey, target);
	}

	public void addMapping(FederationMapping mapKey, TypedPlatformLayerClient localClient) {
		MappedTarget target = new MappedTarget();
		target.client = localClient;

		addMapping(mapKey, target);
	}

	private void addMapping(FederationMapping mapKey, MappedTarget target) {
		if (targetMap.containsKey(mapKey)) {
			throw new IllegalArgumentException("Duplicate key: " + mapKey);
		}

		targetMap.put(mapKey, target);
	}

	private void buildTargetMap(FederationConfiguration config) {
		for (PlatformLayerConnectionConfiguration child : config.systems) {
			FederationKey host = FederationKey.build(child.server);
			ProjectId project = new ProjectId(child.tenant);
			FederationMapping key = new FederationMapping(host, project);

			MappedTarget target = new MappedTarget();
			target.configuration = child;

			addMapping(key, target);
		}
	}

	private void buildRules(FederationConfiguration config) {
		for (FederationRule federationRule : config.rules) {
			Rule rule = new Rule();

			rule.mappedItems = PlatformLayerKey.fromServiceAndItem(federationRule.serviceType, null);

			for (PlatformLayerConnectionConfiguration system : config.systems) {
				if (Objects.equal(system.key, federationRule.target)) {
					if (rule.targetKey != null) {
						throw new IllegalStateException();
					}

					FederationKey host = FederationKey.build(system.server);
					ProjectId project = new ProjectId(system.tenant);
					rule.targetKey = new FederationMapping(host, project);
				}
			}

			if (rule.targetKey == null) {
				throw new IllegalStateException();
			}

			addRule(rule);
		}
	}

	private FederationMapping toKey(PlatformLayerKey original, Rule rule) {
		FederationKey targetHost = original.getHost();
		ProjectId targetProject = original.getProject();

		if (rule.targetKey != null) {
			MappedTarget target = targetMap.get(rule.targetKey);
			if (target == null) {
				throw new IllegalStateException("Cannot find target: " + rule.targetKey);
			}

			targetHost = rule.targetKey.host;
			targetProject = rule.targetKey.project;
		}

		return new FederationMapping(targetHost, targetProject);
	}

	private FederationMapping buildDefault(PlatformLayerKey original) {
		FederationKey targetHost = original.getHost();
		if (targetHost == null) {
			targetHost = FederationKey.LOCAL;
		}

		ProjectId targetProject = original.getProject();

		return new FederationMapping(targetHost, targetProject);
	}

	public Iterable<FederationMapping> getAllTargetKeys() {
		return targetMap.keySet();
	}

	public TypedPlatformLayerClient buildClient(FederationMapping key) {
		MappedTarget target = targetMap.get(key);
		if (target == null) {
			throw new IllegalArgumentException("Unknown key: " + key);
		}

		if (target.client == null) {
			PlatformLayerClient client = DirectPlatformLayerClient.buildUsingConfiguration(target.configuration);

			TypedPlatformLayerClient typedClient = new TypedPlatformLayerClient(client, mapper);
			// TODO: Save client??
			return typedClient;
		} else {
			return target.client;
		}
	}

	public List<FederationMapping> getClients(PlatformLayerKey path) {
		if (path.getHost() != null) {
			throw new IllegalStateException();
		}

		List<FederationMapping> keys = Lists.newArrayList();

		for (Rule rule : rules) {
			if (isMatch(rule, path)) {
				keys.add(toKey(path, rule));
			}
		}

		keys.add(buildDefault(path));

		if (keys.size() > 1) {
			log.debug("Multiple clients for " + path + ": " + Joiner.on(",").join(keys));
		}

		return keys;
	}

	public FederationMapping getClientForCreate(PlatformLayerKey path) {
		if (path.getHost() != null) {
			throw new IllegalStateException();
		}

		List<FederationMapping> keys = Lists.newArrayList();

		for (Rule rule : rules) {
			if (isMatch(rule, path)) {
				keys.add(toKey(path, rule));
			}
		}

		if (keys.isEmpty()) {
			keys.add(buildDefault(path));
		}

		if (keys.size() == 0) {
			return null;
		}

		if (keys.size() != 1) {
			throw new IllegalArgumentException();
		}

		return keys.get(0);
	}

	private boolean isMatch(Rule rule, PlatformLayerKey path) {
		if (rule.mappedItems == null) {
			throw new IllegalStateException();
			// (Used to return true)
		}

		if (rule.mappedItems.getHost() != null) {
			throw new IllegalStateException();
		}

		if (rule.mappedItems.getServiceType() != null) {
			if (!Objects.equal(rule.mappedItems.getServiceType(), path.getServiceType())) {
				return false;
			}
		}

		if (rule.mappedItems.getItemType() != null) {
			if (!Objects.equal(rule.mappedItems.getItemType(), path.getItemType())) {
				return false;
			}
		}

		if (rule.mappedItems.getItemId() != null) {
			throw new IllegalStateException();
		}

		return true;
	}

	public void addRule(Rule rule) {
		rules.add(rule);
	}

}
