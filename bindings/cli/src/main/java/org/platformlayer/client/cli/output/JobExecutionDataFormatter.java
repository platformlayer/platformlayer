package org.platformlayer.client.cli.output;

import java.io.IOException;
import java.util.LinkedHashMap;

import org.platformlayer.jobs.model.JobExecutionData;

import com.fathomdb.cli.formatter.SimpleFormatter;
import com.fathomdb.cli.output.OutputSink;
import com.google.common.collect.Maps;

public class JobExecutionDataFormatter extends SimpleFormatter<JobExecutionData> {

	public JobExecutionDataFormatter() {
		super(JobExecutionData.class);
	}

	@Override
	public void visit(JobExecutionData o, OutputSink sink) throws IOException {
		LinkedHashMap<String, Object> values = Maps.newLinkedHashMap();

		values.put("key", o.getJobKey());
		values.put("executionId", o.getExecutionId());
		values.put("startedAt", o.getStartedAt());
		values.put("endedAt", o.getEndedAt());
		values.put("state", o.getState());

		sink.outputRow(values);
	}
}
