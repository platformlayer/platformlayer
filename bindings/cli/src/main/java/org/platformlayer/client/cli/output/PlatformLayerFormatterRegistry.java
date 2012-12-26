package org.platformlayer.client.cli.output;

import com.fathomdb.cli.formatter.FormatterRegistryBase;

public class PlatformLayerFormatterRegistry extends FormatterRegistryBase {

	public PlatformLayerFormatterRegistry() {
		addDefaultFormatters();

		addFormatter(new TagFormatter());
		addFormatter(new JobDataFormatter());
		addFormatter(new JobExecutionDataFormatter());
		addFormatter(new JobLogLineFormatter());
		addFormatter(new ServiceInfoFormatter());
		addFormatter(new UntypedItemFormatter());
		addFormatter(new MetricDataStreamFormatter());
		addFormatter(new MetricInfoFormatter());
		addFormatter(new MetricValueFormatter());
	}

}
