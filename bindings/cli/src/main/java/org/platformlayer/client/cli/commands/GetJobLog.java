package org.platformlayer.client.cli.commands;

import java.io.PrintWriter;

import org.kohsuke.args4j.Argument;
import org.platformlayer.PlatformLayerClient;
import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.client.cli.autocomplete.AutoCompleteJobId;
import org.platformlayer.common.JobLogLineLevels;
import org.platformlayer.jobs.model.JobLog;
import org.platformlayer.jobs.model.JobLogLine;

import com.fathomdb.cli.autocomplete.AutoCompletor;
import com.fathomdb.cli.autocomplete.SimpleAutoCompleter;
import com.fathomdb.cli.commands.Ansi;

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

		JobLog jobLog = client.getJobExecutionLog(jobId, executionId);
		return jobLog;
	}

	@Override
	public AutoCompletor getAutoCompleter() {
		return new SimpleAutoCompleter(new AutoCompleteJobId());
	}

	@Override
	public void formatRaw(Object o, PrintWriter writer) {
		Ansi ansi = new Ansi(writer);

		JobLog jobLog = (JobLog) o;
		for (JobLogLine line : jobLog) {
			if (line.level >= JobLogLineLevels.LEVEL_ERROR) {
				ansi.setColorRed();
			} else if (line.level >= JobLogLineLevels.LEVEL_WARN) {
				ansi.setColorYellow();
			} else if (line.level >= JobLogLineLevels.LEVEL_INFO) {
				ansi.setColorGreen();
			} else {
				ansi.setColorBlue();
			}

			writer.println(line.message);
			if (line.exception != null) {
				for (String exceptionLine : line.exception.info) {
					writer.println(exceptionLine);
				}
			}
		}

		ansi.reset();
	}

}
