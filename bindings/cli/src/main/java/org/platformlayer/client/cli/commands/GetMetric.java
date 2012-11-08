package org.platformlayer.client.cli.commands;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;
import org.platformlayer.PlatformLayerClient;
import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.client.cli.output.MetricToJsonVisitor;
import org.platformlayer.metrics.model.MetricDataStream;
import org.platformlayer.metrics.model.MetricQuery;

public class GetMetric extends PlatformLayerCommandRunnerBase {
	@Argument(index = 0, required = true, usage = "path")
	public String path;

	// @Argument(index = 1, required = true, usage = "metric key")
	// public String metricKey;

	@Option(name = "-where", usage = "Filter for query")
	public List<String> filters;

	@Option(name = "-select", usage = "'Columns' to select")
	public List<String> projections;

	@Option(name = "-flatten", usage = "Flatten results")
	public boolean flatten = false;

	public GetMetric() {
		super("get", "metric");
	}

	@Override
	public Object runCommand() throws PlatformLayerClientException {
		PlatformLayerClient client = getPlatformLayerClient();

		MetricQuery query = new MetricQuery();
		query.item = getContext().pathToItem(path);
		if (filters != null) {
			query.filters.addAll(filters);
		}

		if (projections != null) {
			query.projections.addAll(projections);
		}

		query.setFlatten(flatten);

		MetricDataStream dataStream = client.getMetric(query);

		return dataStream;
	}

	@Override
	public void formatRaw(Object o, PrintWriter writer) {
		MetricDataStream dataStream = (MetricDataStream) o;

		try {
			MetricToJsonVisitor visitor = new MetricToJsonVisitor(writer);
			dataStream.accept(visitor);
			visitor.close();
		} catch (IOException e) {
			throw new IllegalArgumentException("Error formatting results", e);
		}
	}

}
