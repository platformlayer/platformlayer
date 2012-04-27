package org.platformlayer.client.cli.commands;

import java.util.List;
import java.util.Set;

import org.kohsuke.args4j.Argument;
import org.platformlayer.EndpointInfo;
import org.platformlayer.PlatformLayerClient;
import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.UntypedItem;
import org.platformlayer.client.cli.model.ItemPath;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Tag;
import org.platformlayer.core.model.Tags;

import com.fathomdb.cli.output.ClientAction;
import com.google.common.collect.Sets;

public class SshItem extends PlatformLayerCommandRunnerBase {
	@Argument
	public ItemPath path;

	public SshItem() {
		super("ssh", "item");
	}

	@Override
	public Object runCommand() throws PlatformLayerClientException {
		PlatformLayerClient client = getPlatformLayerClient();

		PlatformLayerKey key = path.resolve(getContext());

		UntypedItem untypedItem = client.getItemUntyped(key);
		Tags itemTags = untypedItem.getTags();
		List<EndpointInfo> endpointList = EndpointInfo.getEndpoints(itemTags);

		Set<EndpointInfo> endpoints = Sets.newHashSet(endpointList);

		EndpointInfo bestEndpoint = null;
		for (EndpointInfo candidate : endpoints) {
			if (bestEndpoint == null) {
				bestEndpoint = candidate;
			} else {
				throw new IllegalArgumentException("Cannot choose between: " + bestEndpoint + " and " + candidate);
			}
		}

		String host = null;

		if (bestEndpoint != null) {
			host = bestEndpoint.publicIp;
		}

		if (host == null) {
			String addressTag = itemTags.findUnique(Tag.NETWORK_ADDRESS);
			if (addressTag != null) {
				int colonIndex = addressTag.indexOf(':');
				if (colonIndex != -1) {
					host = addressTag.substring(colonIndex);
				}
			}
		}

		ClientAction action = null;
		if (host != null) {
			// Hmmm... user? key?
			// action = new ClientAction(ClientAction.ClientActionType.SSH, "root@" + bestEndpoint);
		}

		return action;
	}
}
