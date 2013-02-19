package org.platformlayer.client.cli.output;

import java.io.IOException;
import java.util.LinkedHashMap;

import org.platformlayer.metrics.model.MetricDataStream;

import com.fathomdb.cli.CliContext;
import com.fathomdb.cli.formatter.SimpleFormatter;
import com.fathomdb.cli.output.OutputSink;
import com.google.common.collect.Maps;

public class MetricDataStreamFormatter extends SimpleFormatter<MetricDataStream> {

	public MetricDataStreamFormatter() {
		super(MetricDataStream.class);
	}

	@Override
	public void visit(CliContext context, MetricDataStream o, OutputSink sink) throws IOException {
		LinkedHashMap<String, Object> values = Maps.newLinkedHashMap();

		// values.put("time", o.getTime());
		values.put("value", MetricToJsonVisitor.toString(o));

		sink.outputRow(values);
	}

}
