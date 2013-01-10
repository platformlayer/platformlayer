package org.platformlayer.client.cli.commands;

import java.io.PrintWriter;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.platformlayer.PlatformLayerClient;
import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.common.JobState;
import org.platformlayer.jobs.model.JobExecutionData;
import org.platformlayer.jobs.model.JobExecutionList;
import org.platformlayer.jobs.model.JobLog;
import org.platformlayer.jobs.model.JobLogLine;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class TailLog extends PlatformLayerCommandRunnerBase {
	@Argument(index = 0)
	String jobId;

	@Argument(index = 1)
	String executionId;

	public TailLog() {
		super("tail", "log");
	}

	@Override
	public Object runCommand() throws PlatformLayerClientException, InterruptedException {
		PlatformLayerClient client = getPlatformLayerClient();

		List<JobLog> logs = Lists.newArrayList();

		if (Strings.isNullOrEmpty(executionId)) {
			JobExecutionList jobExecutions = client.listJobExecutions(jobId);

			JobExecutionData last = null;

			// TODO: Fix 1+N slowness...
			for (JobExecutionData execution : jobExecutions.getRuns()) {
				if (execution.getState() == JobState.PRESTART) {
					continue;
				}

				if (last == null) {
					last = execution;
					continue;
				}

				if (last.getStartedAt().before(execution.getStartedAt())) {
					last = execution;
					continue;
				}
				// String executionId = execution.getExecutionId();
				// try {
				// JobLog jobLog = client.getJobExecutionLog(jobId, executionId);
				// logs.add(jobLog);
				// } catch (PlatformLayerClientNotFoundException e) {
				// // TODO: Warn?
				// }
			}

			if (last != null) {
				executionId = last.getExecutionId();
			}
		}

		while (true) {
			// TODO: Only fetch tail
			JobLog jobLog = client.getJobExecutionLog(jobId, executionId);

			List<JobLogLine> lines = jobLog.getLines();
			for (JobLogLine line : lines) {
				System.out.println(line.getMessage());
			}
			Thread.sleep(1000);
		}
	}

	@Override
	public void formatRaw(Object o, PrintWriter writer) {
		// Ansi ansi = new Ansi(writer);
		//
		// List<JobLog> jobLogs = (List<JobLog>) o;
		// for (JobLog jobLog : jobLogs) {
		// int depth = 0;
		// String indent = "";
		//
		// for (JobLogLine line : jobLog) {
		// String type = line.getType();
		//
		// if (!Strings.isNullOrEmpty(type)) {
		// if (type.equals(JobLogLine.TYPE_ENTER_SCOPE)) {
		// writer.println(indent + ">>> " + line.message);
		// depth++;
		// indent += "  ";
		// } else if (type.equals(JobLogLine.TYPE_EXIT_SCOPE)) {
		// depth--;
		// indent = indent.substring(0, depth * 2);
		// // writer.println(indent + "<<< " + line.message);
		// } else {
		// writer.println(indent + "??? " + line.message);
		// }
		// continue;
		// }
		//
		// if (line.level >= JobLogLineLevels.LEVEL_ERROR) {
		// ansi.setColorRed();
		// } else if (line.level >= JobLogLineLevels.LEVEL_WARN) {
		// ansi.setColorYellow();
		// } else if (line.level >= JobLogLineLevels.LEVEL_INFO) {
		// ansi.setColorGreen();
		// } else {
		// ansi.setColorBlue();
		// }
		//
		// writer.print(indent);
		// writer.println(line.message);
		//
		// JobLogExceptionInfo exceptionInfo = line.exception;
		// while (exceptionInfo != null) {
		// for (String exceptionLine : exceptionInfo.info) {
		// writer.print(indent);
		// writer.println(exceptionLine);
		// }
		//
		// exceptionInfo = exceptionInfo.inner;
		// }
		// }
		// }
		//
		// ansi.reset();
	}

}
