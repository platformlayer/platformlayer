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

		// TODO: System.out isn't quite right
		JobLogPrinter jobLogPrinter = new JobLogPrinter(new PrintWriter(System.out));

		if (Strings.isNullOrEmpty(executionId)) {
			JobExecutionList jobExecutions = client.listJobExecutions(jobId);

			JobExecutionData last = null;

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
			}

			if (last != null) {
				executionId = last.getExecutionId();
			}
		}

		// TODO: What if executionId == null? Also retries..
		JobLog previousJobLog = null;
		int jobLogOffset = 0;

		while (true) {
			// TODO: Only fetch tail
			JobLog jobLog = client.getJobExecutionLog(jobId, executionId);
			if (previousJobLog == null) {
				jobLogPrinter.startJobLog(jobLog);
			}

			List<JobLogLine> lines = jobLog.getLines();

			if (jobLogOffset < lines.size()) {
				for (JobLogLine line : lines.subList(jobLogOffset, lines.size())) {
					jobLogPrinter.write(line);
				}
			}

			jobLogPrinter.flush();

			jobLogOffset = lines.size();
			previousJobLog = jobLog;

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
