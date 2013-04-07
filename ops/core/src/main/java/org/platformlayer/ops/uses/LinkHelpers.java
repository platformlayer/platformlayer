package org.platformlayer.ops.uses;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.Link;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.helpers.ProviderHelper;
import org.platformlayer.ops.machines.PlatformLayerHelpers;

import com.fathomdb.properties.PropertyUtils;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;

public class LinkHelpers {

	@Inject
	ProviderHelper providers;

	@Inject
	PlatformLayerHelpers platformLayer;

	// public InetAddressChooser inetAddressChooser = InetAddressChooser.preferIpv6();

	public Map<String, String> buildLinkTargetProperties(LinkConsumer consumer, List<Link> links) throws OpsException {
		Map<String, String> config = Maps.newHashMap();

		if (links != null) {

			for (Link link : links) {
				ItemBase item = platformLayer.getItem(link.getTarget());
				LinkTarget linkTarget = providers.toInterface(item, LinkTarget.class);

				Map<String, String> linkTargetConfig = buildLinkTargetConfiguration(consumer, link.name, linkTarget);
				config.putAll(linkTargetConfig);
			}
		}

		return config;
	}

	private Map<String, String> buildLinkTargetConfiguration(LinkConsumer consumer, String linkName,
			LinkTarget linkTarget) throws OpsException {
		Map<String, String> config = linkTarget.buildLinkTargetConfiguration(consumer);

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
