package org.platformlayer.client.cli.output;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

import org.platformlayer.jobs.model.JobExecutionData;
import org.platformlayer.jobs.model.JobLog;
import org.platformlayer.jobs.model.JobLogLine;

import com.fathomdb.cli.CliContext;
import com.fathomdb.cli.formatter.SimpleFormatter;
import com.fathomdb.cli.output.OutputSink;
import com.google.common.collect.Maps;

public class JobLogFormatter extends SimpleFormatter<JobLog> {

	public JobLogFormatter() {
		super(JobLog.class);
	}

	@Override
	public void visit(CliContext context, JobLog o, OutputSink sink) throws IOException {
		LinkedHashMap<String, Object> values = Maps.newLinkedHashMap();

		JobExecutionData execution = o.getExecution();
		if (execution != null) {
			values.put("jobId", execution.getJobKey().getItemIdString());
			values.put("executionId", execution.getExecutionId());

			values.put("start", o.getExecution().getStartedAt());
			values.put("end", o.getExecution().getEndedAt());

			values.put("state", execution.getState());
		}

		List<JobLogLine> lines = o.getLines();
		values.put("lines", lines.size());

		sink.outputRow(values);
	}
}
