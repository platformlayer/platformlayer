package org.platformlayer.client.cli.commands;

import java.io.PrintWriter;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.platformlayer.Format;
import org.platformlayer.PlatformLayerClient;
import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.client.cli.model.ItemPath;
import org.platformlayer.common.UntypedItem;
import org.platformlayer.core.model.EndpointInfo;
import org.platformlayer.core.model.PlatformLayerKey;

public class GetEndpoint extends PlatformLayerCommandRunnerBase {
	@Argument
	public ItemPath path;

	public GetEndpoint() {
		super("get", "endpoint");
	}

	@Override
	public Object runCommand() throws PlatformLayerClientException {
		// Should this be a tag?
		PlatformLayerClient client = getPlatformLayerClient();

		PlatformLayerKey key = path.resolve(getContext());

		UntypedItem untypedItem = client.getItemUntyped(key, Format.XML);
		List<EndpointInfo> endpoints = EndpointInfo.getEndpoints(untypedItem.getTags());

		return endpoints;
	}

	@Override
	public void formatRaw(Object o, PrintWriter writer) {
		if (o == null) {
			return;
		}

		List<EndpointInfo> endpoints = (List<EndpointInfo>) o;
		for (EndpointInfo s : endpoints) {
			writer.println(s.toString());
		}
	}

}
