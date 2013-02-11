package org.platformlayer.client.cli.commands;

import org.kohsuke.args4j.Argument;
import org.platformlayer.PlatformLayerClient;
import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.metrics.model.MetricInfoCollection;

public class ListMetrics extends PlatformLayerCommandRunnerBase {
	@Argument(index = 0)
	public String path;

	public ListMetrics() {
		super("list", "metrics");
	}

	@Override
	public Object runCommand() throws PlatformLayerClientException {
		PlatformLayerClient client = getPlatformLayerClient();

		PlatformLayerKey key = getContext().pathToItem(getProject(), path);

		MetricInfoCollection items = client.listMetrics(key);

		return items;
	}

}
