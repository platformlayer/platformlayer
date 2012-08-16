package org.platformlayer.client.cli.commands;

import org.kohsuke.args4j.Argument;
import org.platformlayer.PlatformLayerClient;
import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.metrics.model.MetricDataStream;

public class GetMetric extends PlatformLayerCommandRunnerBase {
	@Argument(index = 0, required = true, usage = "path")
	public String path;

	@Argument(index = 1, required = true, usage = "metric key")
	public String metricKey;

	public GetMetric() {
		super("get", "metric");
	}

	@Override
	public Object runCommand() throws PlatformLayerClientException {
		PlatformLayerClient client = getPlatformLayerClient();

		PlatformLayerKey key = getContext().pathToItem(path);

		MetricDataStream dataStream = client.getMetric(key, metricKey);

		return dataStream;
	}
}
