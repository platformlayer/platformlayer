package org.platformlayer.client.cli.output;

import java.io.IOException;
import java.util.LinkedHashMap;

import org.platformlayer.jobs.model.JobData;

import com.fathomdb.cli.CliContext;
import com.fathomdb.cli.formatter.SimpleFormatter;
import com.fathomdb.cli.output.OutputSink;
import com.google.common.collect.Maps;

public class JobDataFormatter extends SimpleFormatter<JobData> {

	public JobDataFormatter() {
		super(JobData.class);
	}

	@Override
	public void visit(CliContext context, JobData o, OutputSink sink) throws IOException {
		LinkedHashMap<String, Object> values = Maps.newLinkedHashMap();

		values.put("id", o.key.getItemIdString());
		values.put("target", o.targetId);
		values.put("action", o.action.getType());
		// values.put("state", o.state);

		sink.outputRow(values);
	}
}
