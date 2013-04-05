package org.platformlayer.ops.uses;

import java.util.Map;

import javax.inject.Inject;

import org.platformlayer.InetAddressChooser;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.Link;
import org.platformlayer.core.model.Links;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.helpers.ProviderHelper;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.networks.NearestAddressChooser;
import org.platformlayer.ops.networks.NetworkPoint;

import com.fathomdb.properties.PropertyUtils;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;

public class LinkHelpers {

	@Inject
	ProviderHelper providers;

	@Inject
	PlatformLayerHelpers platformLayer;

	// public InetAddressChooser inetAddressChooser = InetAddressChooser.preferIpv6();

	public Map<String, String> buildLinkTargetProperties(Links links) throws OpsException {
		Map<String, String> config = Maps.newHashMap();

		if (links != null) {
			NetworkPoint networkPoint = NetworkPoint.forTargetInContext();
			InetAddressChooser inetAddressChooser = NearestAddressChooser.build(networkPoint);

			for (Link link : links.getLinks()) {
				ItemBase item = platformLayer.getItem(link.getTarget());
				LinkTarget linkTarget = providers.toInterface(item, LinkTarget.class);

				Map<String, String> linkTargetConfig = buildLinkTargetConfiguration(link.name, linkTarget,
						inetAddressChooser);
				config.putAll(linkTargetConfig);
			}
		}

		return config;
	}

	private Map<String, String> buildLinkTargetConfiguration(String linkName, LinkTarget linkTarget,
			InetAddressChooser inetAddressChooser) throws OpsException {
		Map<String, String> config = linkTarget.buildLinkTargetConfiguration(inetAddressChooser);

		if (config == null) {
			config = Maps.newHashMap();
		}

		if (linkTarget.getCaForClientKey() != null) {
			String alias = buildKeyName(linkName);
			config.put("tls.clientcert", alias);
		}

		if (!Strings.isNullOrEmpty(linkName)) {
			String prefix = linkName + ".";
			config = PropertyUtils.prefixProperties(config, prefix);
		}

		return config;
	}

	/**
	 * Deprecated: Prefer using LinkTargets instead
	 * 
	 * @param properties
	 * @param linkName
	 * @param key
	 * @throws OpsException
	 */
	@Deprecated
	public void addTarget(Map<String, String> properties, String linkName, PlatformLayerKey key) throws OpsException {
		ItemBase item = platformLayer.findItem(key);
		LinkTarget linkTarget = providers.toInterface(item, LinkTarget.class);

		NetworkPoint networkPoint = NetworkPoint.forTargetInContext();

		InetAddressChooser inetAddressChooser = NearestAddressChooser.build(networkPoint);
		Map<String, String> config = buildLinkTargetConfiguration(linkName, linkTarget, inetAddressChooser);

		properties.putAll(config);
	}

	public String buildKeyName(Link link) {
		return buildKeyName(link.name);
	}

	public String buildKeyName(String linkName) {
		if (Strings.isNullOrEmpty(linkName)) {
			throw new IllegalStateException();
		}
		String alias = "tls.client." + linkName;
		return alias;
	}
}
