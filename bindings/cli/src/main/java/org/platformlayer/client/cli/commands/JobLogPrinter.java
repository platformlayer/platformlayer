package org.platformlayer.client.cli.commands;

import java.io.PrintWriter;
import java.util.List;

import org.platformlayer.common.JobLogLineLevels;
import org.platformlayer.jobs.model.JobLog;
import org.platformlayer.jobs.model.JobLogExceptionInfo;
import org.platformlayer.jobs.model.JobLogLine;

import com.fathomdb.cli.commands.Ansi;
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
				writer.println(indent + ">>> " + line.message);
				depth++;
				indent += "  ";
			} else if (type.equals(JobLogLine.TYPE_EXIT_SCOPE)) {
				depth--;
				indent = indent.substring(0, depth * 2);
				// writer.println(indent + "<<< " + line.message);
			} else {
				writer.println(indent + "??? " + line.message);
			}
			return;
		}

		if (line.level >= JobLogLineLevels.LEVEL_ERROR) {
			ansi.setColorRed();
		} else if (line.level >= JobLogLineLevels.LEVEL_WARN) {
			ansi.setColorYellow();
		} else if (line.level >= JobLogLineLevels.LEVEL_INFO) {
			ansi.setColorGreen();
		} else {
			ansi.setColorBlue();
		}

		writer.print(indent);
		writer.println(line.message);

		JobLogExceptionInfo exceptionInfo = line.exception;
		while (exceptionInfo != null) {
			for (String exceptionLine : exceptionInfo.info) {
				writer.print(indent);
				writer.println(exceptionLine);
			}

			exceptionInfo = exceptionInfo.inner;
		}
	}

	public void flush() {
		writer.flush();
	}

}
