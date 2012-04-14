package org.platformlayer.client.cli.output;

import java.io.IOException;
import java.util.LinkedHashMap;

import org.platformlayer.jobs.model.JobLogLine;

import com.fathomdb.cli.formatter.SimpleFormatter;
import com.fathomdb.cli.output.OutputSink;
import com.google.common.collect.Maps;

public class JobLogLineFormatter extends SimpleFormatter<JobLogLine> {

	public JobLogLineFormatter() {
		super(JobLogLine.class);
	}

	@Override
	public void visit(JobLogLine o, OutputSink sink) throws IOException {
		LinkedHashMap<String, Object> values = Maps.newLinkedHashMap();

		values.put("timestamp", o.timestamp);
		values.put("level", o.level);
		values.put("message", o.message);
		values.put("exception", o.exception);

		sink.outputRow(values);
	}
}
