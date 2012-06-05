package org.platformlayer.client.cli.commands;

import java.util.List;
import java.util.Set;

import org.kohsuke.args4j.Argument;
import org.platformlayer.PlatformLayerClient;
import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.UntypedItem;
import org.platformlayer.client.cli.model.ItemPath;
import org.platformlayer.core.model.EndpointInfo;
import org.platformlayer.core.model.PlatformLayerKey;

import com.fathomdb.cli.output.ClientAction;
import com.google.common.collect.Sets;

public class OpenItem extends PlatformLayerCommandRunnerBase {
	@Argument
	public ItemPath path;

	public OpenItem() {
		super("open", "item");
	}

	@Override
	public Object runCommand() throws PlatformLayerClientException {
		PlatformLayerClient client = getPlatformLayerClient();

		PlatformLayerKey key = path.resolve(getContext());

		UntypedItem untypedItem = client.getItemUntyped(key);
		List<EndpointInfo> endpointList = EndpointInfo.getEndpoints(untypedItem.getTags());

		Set<EndpointInfo> endpoints = Sets.newHashSet(endpointList);

		EndpointInfo bestEndpoint = null;
		for (EndpointInfo candidate : endpoints) {
			if (bestEndpoint == null) {
				bestEndpoint = candidate;
			} else {
				throw new IllegalArgumentException("Cannot choose between: " + bestEndpoint + " and " + candidate);
			}
		}

		ClientAction action = null;

		if (bestEndpoint != null) {
			// TODO: How do we want to do this? A new tag??
			String id = key.getServiceType().getKey() + ":" + key.getItemType().getKey();
			if (id.equals("jenkins:jenkinsService")) {
				action = new ClientAction(ClientAction.ClientActionType.BROWSER, "http://" + bestEndpoint.publicIp
						+ ":" + bestEndpoint.publicIp);
			}
		}

		return action;
	}

}
