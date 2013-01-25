package org.platformlayer.client.cli.commands;

import org.kohsuke.args4j.Argument;
import org.platformlayer.PlatformLayerClient;
import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.client.cli.model.ItemPath;
import org.platformlayer.common.UntypedItem;
import org.platformlayer.core.model.PlatformLayerKey;

public class ListTags extends PlatformLayerCommandRunnerBase {
	@Argument(index = 0)
	public ItemPath path;

	public ListTags() {
		super("list", "tags");
	}

	@Override
	public Object runCommand() throws PlatformLayerClientException {
		PlatformLayerClient client = getPlatformLayerClient();

		PlatformLayerKey key = path.resolve(getContext());
		UntypedItem ret = client.getItemUntyped(key, getFormat());

		return ret.getTags();
	}
}
