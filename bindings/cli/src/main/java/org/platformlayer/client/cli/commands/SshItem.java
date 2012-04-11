package org.platformlayer.client.cli.commands;

import java.util.List;
import java.util.Set;

import org.kohsuke.args4j.Argument;
import org.platformlayer.PlatformLayerClient;
import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.PlatformLayerUtils;
import org.platformlayer.UntypedItem;
import org.platformlayer.client.cli.model.ItemPath;
import org.platformlayer.core.model.PlatformLayerKey;

import com.fathomdb.cli.output.ClientAction;
import com.google.common.collect.Lists;
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
		List<String> endpointList = PlatformLayerUtils.findEndpoints(untypedItem.getTags());

		Set<String> endpoints = Sets.newHashSet(endpointList);

		String bestEndpoint = null;
		for (String candidate : endpoints) {
			if (bestEndpoint == null) {
				bestEndpoint = candidate;
			} else {
				throw new IllegalArgumentException("Cannot choose between: " + bestEndpoint + " and " + candidate);
			}
		}

		ClientAction action = null;

		if (bestEndpoint != null) {
			String host = bestEndpoint;
			int colonIndex = host.indexOf(':');
			if (colonIndex != -1) {
				host = host.substring(colonIndex);
			}

			// Hmmm... user? key?
			// action = new ClientAction(ClientAction.ClientActionType.SSH, "root@" + bestEndpoint);
		}

		return action;
	}

}
