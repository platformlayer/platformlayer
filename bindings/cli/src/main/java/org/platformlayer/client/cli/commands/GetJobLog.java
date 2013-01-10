package org.platformlayer.client.cli.commands;

import java.io.PrintWriter;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.platformlayer.PlatformLayerClient;
import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.PlatformLayerClientNotFoundException;
import org.platformlayer.client.cli.autocomplete.AutoCompleteJobId;
import org.platformlayer.common.JobState;
import org.platformlayer.jobs.model.JobExecutionData;
import org.platformlayer.jobs.model.JobExecutionList;
import org.platformlayer.jobs.model.JobLog;

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

		List<JobLog> logs = Lists.newArrayList();

		if (Strings.isNullOrEmpty(executionId)) {
			JobExecutionList jobExecutions = client.listJobExecutions(jobId);

			// TODO: Fix 1+N slowness...
			for (JobExecutionData execution : jobExecutions.getRuns()) {
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
