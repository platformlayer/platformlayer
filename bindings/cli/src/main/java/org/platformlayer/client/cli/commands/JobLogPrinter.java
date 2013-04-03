package org.platformlayer.client.cli.commands;

import java.io.PrintWriter;
import java.util.List;

import org.platformlayer.jobs.model.JobLog;
import org.platformlayer.jobs.model.JobLogExceptionInfo;
import org.platformlayer.jobs.model.JobLogLine;
import org.platformlayer.jobs.model.JobLogLineLevels;

import com.fathomdb.cli.commands.Ansi;
import com.fathomdb.cli.commands.Ansi.Color;
import com.google.common.base.Strings;

public class JobLogPrinter {

	private final PrintWriter writer;
	private final Ansi ansi;

	int depth;
	String indent;

	public JobLogPrinter(PrintWriter writer) {
		this.writer = writer;
		this.ansi = new Ansi(writer);
	}

	public void write(List<JobLog> jobLogs) {
		for (JobLog jobLog : jobLogs) {
			write(jobLog);
		}

	}

	private void write(JobLog jobLog) {
		startJobLog(jobLog);

		for (JobLogLine line : jobLog) {
			write(line);
		}

		endJobLog(jobLog);
	}

	private void endJobLog(JobLog jobLog) {

	}

	public void startJobLog(JobLog jobLog) {
		depth = 0;
		indent = "";
	}

	public void end() {
		ansi.reset();

		flush();
	}

	public void write(JobLogLine line) {
		String type = line.getType();

		if (!Strings.isNullOrEmpty(type)) {
			if (type.equals(JobLogLine.TYPE_ENTER_SCOPE)) {
				ansi.println(Color.Default, indent + ">>> " + line.message);
				depth++;
				indent += "  ";
			} else if (type.equals(JobLogLine.TYPE_EXIT_SCOPE)) {
				depth--;
				indent = indent.substring(0, depth * 2);
				// ansi.println(indent + "<<< " + line.message);
			} else {
				ansi.println(Color.Red, indent + "??? " + line.message);
			}
			return;
		}

		Ansi.Color color = Ansi.Color.Default;

		if (line.level >= JobLogLineLevels.LEVEL_ERROR) {
			color = Ansi.Color.Red;
		} else if (line.level >= JobLogLineLevels.LEVEL_WARN) {
			color = Ansi.Color.Yellow;
		} else if (line.level >= JobLogLineLevels.LEVEL_INFO) {
			color = Ansi.Color.Green;
		} else {
			color = Ansi.Color.Blue;
		}

		ansi.println(color, indent + line.message);

		JobLogExceptionInfo exceptionInfo = line.exception;
		while (exceptionInfo != null) {
			for (String exceptionLine : exceptionInfo.info) {
				ansi.println(color, indent + exceptionLine);
			}

			exceptionInfo = exceptionInfo.inner;
		}
	}

	public void flush() {
		writer.flush();
	}

}
