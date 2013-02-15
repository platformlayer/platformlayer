package org.platformlayer.ops.uses;

import java.util.Map;

import javax.inject.Inject;

import org.platformlayer.InetAddressChooser;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.Link;
import org.platformlayer.core.model.Links;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.helpers.ProviderHelper;
import org.platformlayer.ops.machines.PlatformLayerHelpers;

import com.google.common.collect.Maps;

public class ConsumeHelpers {

	@Inject
	ProviderHelper providerHelper;
	@Inject
	PlatformLayerHelpers platformLayer;

	public InetAddressChooser inetAddressChooser = InetAddressChooser.preferIpv6();

	public Map<String, String> buildConfigProperties(Links links) throws OpsException {
		Map<String, String> config = Maps.newHashMap();

		if (links != null) {
			for (Link link : links.getLinks()) {
				ItemBase item = platformLayer.getItem(link.getTarget());
				Consumable consumable = providerHelper.toInterface(item, Consumable.class);

				Map<String, String> consumableConfig = consumable.buildConsumerConfiguration(inetAddressChooser);
				if (consumableConfig != null) {
					config.putAll(consumableConfig);
				}
			}
		}

		return config;
	}

}
