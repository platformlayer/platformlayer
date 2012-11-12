package org.platformlayer.ops.jobstore;

import java.util.List;

import org.platformlayer.AppendOnlyList;
import org.platformlayer.jobs.model.JobLogExceptionInfo;
import org.platformlayer.jobs.model.JobLogLine;
import org.platformlayer.ops.log.JobLogger;
import org.platformlayer.ops.log.JobUtils;

public class SimpleJobLogger implements JobLogger {
	final AppendOnlyList<JobLogLine> lines = AppendOnlyList.create();

	public SimpleJobLogger() {
	}

	@Override
	public void logMessage(String message, String[] exceptionInfo, int level) {
		JobLogExceptionInfo jobLogExceptionInfo = null;
		if (exceptionInfo != null) {
			jobLogExceptionInfo = JobUtils.buildJobLogExceptionInfo(exceptionInfo);
		}

		JobLogLine jobLogLine = new JobLogLine(System.currentTimeMillis(), level, message, jobLogExceptionInfo);
		lines.add(jobLogLine);
	}

	public Iterable<JobLogLine> getLogEntries() {
		// TODO: We might need a special iterator here that tolerates concurrent modifications
		// class AppendOnlyListIterator implements Iterator<T> {
		// final int size;
		// int position = -1;
		//
		// AppendOnlyListIterator() {
		// size = list.size();
		// }
		//
		// public boolean hasNext() {
		// if ((position + 1) < size)
		// return true;
		// return false;
		// }
		//
		// public T next() {
		// position++;
		// return list.get(position);
		// }
		//
		// public void remove() {
		// throw new UnsupportedOperationException();
		// }
		// }
		//
		//
		return lines;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (JobLogLine jobLogLine : this.getLogEntries()) {
			sb.append(jobLogLine.getMessage());

			JobLogExceptionInfo exception = jobLogLine.getException();

			if (exception != null) {
				List<String> infos = exception.getInfo();

				for (int i = 0; i < infos.size(); i++) {
					String infoLine = infos.get(i);
					if (i != 0) {
						sb.append('\n');
					}
					sb.append(infoLine);
				}
			}

			sb.append("\n");
		}
		return sb.toString();
	}

	@Override
	public void enterScope(Object controller) {
		String name = ControllerNameStrategy.getName(controller);
		JobLogLine line = new JobLogLine();
		line.type = JobLogLine.TYPE_ENTER_SCOPE;
		line.message = name;
		lines.add(line);
	}

	@Override
	public void exitScope() {
		JobLogLine line = new JobLogLine();
		line.type = JobLogLine.TYPE_EXIT_SCOPE;
		lines.add(line);
	}

}
