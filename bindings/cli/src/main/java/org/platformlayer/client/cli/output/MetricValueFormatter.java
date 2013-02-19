package org.platformlayer.client.cli.output;

import java.io.IOException;
import java.util.LinkedHashMap;

import org.platformlayer.metrics.model.MetricValue;

import com.fathomdb.cli.CliContext;
import com.fathomdb.cli.formatter.SimpleFormatter;
import com.fathomdb.cli.output.OutputSink;
import com.google.common.collect.Maps;

public class MetricValueFormatter extends SimpleFormatter<MetricValue> {

	public MetricValueFormatter() {
		super(MetricValue.class);
	}

	@Override
	public void visit(CliContext context, MetricValue o, OutputSink sink) throws IOException {
		LinkedHashMap<String, Object> values = Maps.newLinkedHashMap();

		values.put("time", o.getTime());
		values.put("value", o.getValue());

		sink.outputRow(values);
	}

}
