package org.platformlayer.ops.log;

import java.util.List;

import org.platformlayer.jobs.model.JobLog;
import org.platformlayer.jobs.model.JobLogExceptionInfo;
import org.platformlayer.jobs.model.JobLogLine;

public class JobLogger {
	final JobLog jobLog;

	public JobLogger(JobLog jobLog) {
		this.jobLog = jobLog;
	}

	public void logMessage(String message, String[] exceptionInfo, int level) {
		JobLogExceptionInfo jobLogExceptionInfo = null;
		if (exceptionInfo != null) {
			jobLogExceptionInfo = JobUtils.buildJobLogExceptionInfo(exceptionInfo);
		}

		JobLogLine jobLogLine = new JobLogLine(System.currentTimeMillis(), level, message, jobLogExceptionInfo);
		jobLog.lines.add(jobLogLine);
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
		return jobLog.getLines();
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

}
