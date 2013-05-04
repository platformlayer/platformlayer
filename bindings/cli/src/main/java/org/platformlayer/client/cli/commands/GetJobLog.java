package org.platformlayer.client.cli.commands;

import java.io.PrintWriter;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.platformlayer.PlatformLayerClient;
import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.PlatformLayerClientNotFoundException;
import org.platformlayer.client.cli.autocomplete.AutoCompleteJobId;
import org.platformlayer.jobs.model.JobExecutionData;
import org.platformlayer.jobs.model.JobExecutionList;
import org.platformlayer.jobs.model.JobLog;
import org.platformlayer.jobs.model.JobState;

import com.fathomdb.cli.autocomplete.AutoCompletor;
import com.fathomdb.cli.autocomplete.SimpleAutoCompleter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class GetJobLog extends PlatformLayerCommandRunnerBase {
	@Argument(index = 0)
	String jobId;

	@Argument(index = 1)
	String executionId;

	public GetJobLog() {
		super("get", "log");
	}

	@Override
	public Object runCommand() throws PlatformLayerClientException {
		PlatformLayerClient client = getPlatformLayerClient();

		if (jobId.contains("/") && executionId == null) {
			String[] tokens = jobId.split("/");
			if (tokens.length == 2) {
				jobId = tokens[0];
				executionId = tokens[1];
			}
		}
		List<JobLog> logs = Lists.newArrayList();

		if (Strings.isNullOrEmpty(executionId)) {
			JobExecutionList jobExecutions = client.listJobExecutions(jobId);

			List<JobExecutionData> runs = jobExecutions.getRuns();

			sort(runs);

			// TODO: Remove limit (or iterate)
			if (runs.size() > 10) {
				runs = Lists.newArrayList(runs.subList(runs.size() - 10, runs.size()));
			}

			// TODO: Fix 1+N slowness...
			for (JobExecutionData execution : runs) {
				if (execution.getState() == JobState.PRESTART) {
					continue;
				}

				String executionId = execution.getExecutionId();
				try {
					JobLog jobLog = client.getJobExecutionLog(jobId, executionId);
					logs.add(jobLog);
				} catch (PlatformLayerClientNotFoundException e) {
					// TODO: Warn?
				}
			}
		} else {
			JobLog jobLog = client.getJobExecutionLog(jobId, executionId);
			logs.add(jobLog);
		}

		return logs;
	}

	@Override
	public AutoCompletor getAutoCompleter() {
		return new SimpleAutoCompleter(new AutoCompleteJobId());
	}

	@Override
	public void formatRaw(Object o, PrintWriter writer) {
		JobLogPrinter printer = new JobLogPrinter(writer);
		List<JobLog> jobLogs = (List<JobLog>) o;

		printer.write(jobLogs);

		printer.end();
	}

}
