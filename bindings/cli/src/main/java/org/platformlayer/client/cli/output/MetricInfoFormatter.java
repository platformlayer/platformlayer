package org.platformlayer.client.cli.output;

import java.io.IOException;
import java.util.LinkedHashMap;

import org.platformlayer.metrics.model.MetricInfo;

import com.fathomdb.cli.formatter.SimpleFormatter;
import com.fathomdb.cli.output.OutputSink;
import com.google.common.collect.Maps;

public class MetricInfoFormatter extends SimpleFormatter<MetricInfo> {

	public MetricInfoFormatter() {
		super(MetricInfo.class);
	}

	@Override
	public void visit(MetricInfo o, OutputSink sink) throws IOException {
		LinkedHashMap<String, Object> values = Maps.newLinkedHashMap();

		values.put("key", o.getKey());
		values.put("description", o.getDescription());

		sink.outputRow(values);
	}

}
