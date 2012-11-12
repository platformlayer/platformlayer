package org.platformlayer.ops.jobstore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.inject.Singleton;

import org.platformlayer.IoUtils;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.jobs.model.JobLog;
import org.platformlayer.jobs.model.JobLogExceptionInfo;
import org.platformlayer.jobs.model.JobLogLine;
import org.platformlayer.ops.jobstore.protobuf.JobDataProtobuf;
import org.platformlayer.ops.log.JobLogger;

import com.google.common.collect.Lists;
import com.google.common.io.Closeables;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;

@Singleton
public class FilesystemJobLogStore implements JobLogStore {
	final File baseDir;

	public FilesystemJobLogStore(File baseDir) {
		this.baseDir = baseDir;
	}

	@Override
	public JobLog getJobLog(Date startTime, PlatformLayerKey jobKey, String executionId, int logSkip)
			throws IOException {
		File file = toFile(startTime, jobKey, executionId);
		if (!file.exists()) {
			return null;
		}

		ArrayList<JobLogLine> lines = Lists.newArrayList();

		InputStream is = null;
		CodedInputStream in = null;
		try {
			is = new FileInputStream(file);
			is = new GZIPInputStream(is);
			in = CodedInputStream.newInstance(is);

			int i = 0;
			JobDataProtobuf.JobLogLine.Builder protobuf = JobDataProtobuf.JobLogLine.newBuilder();
			while (!in.isAtEnd()) {
				int length = in.readRawVarint32();
				if (i < logSkip) {
					in.skipRawBytes(length);
				} else {
					int oldLimit = in.pushLimit(length);

					protobuf.clear();
					protobuf.mergeFrom(in);

					JobLogLine line = new JobLogLine();
					line.level = protobuf.getLevel();
					line.timestamp = protobuf.getTimestamp();
					line.message = protobuf.getMessage();
					line.type = protobuf.getType();
					if (protobuf.hasException()) {
						line.exception = mapFromProtobuf(protobuf.getExceptionBuilder());
					}

					lines.add(line);

					in.popLimit(oldLimit);
				}

				i++;
			}

		} finally {
			// Closeables.closeQuietly(in);
			Closeables.closeQuietly(is);
		}

		JobLog jobLog = new JobLog();
		jobLog.lines = lines;
		return jobLog;
	}

	@Override
	public void saveJobLog(PlatformLayerKey jobKey, String executionId, Date endTime, JobLogger logger)
			throws IOException {
		File file = toFile(endTime, jobKey, executionId);
		if (file.exists()) {
			throw new IllegalStateException();
		}

		boolean okay = false;
		OutputStream os = null;
		CodedOutputStream out = null;
		try {
			os = new FileOutputStream(file);
			os = new GZIPOutputStream(os);
			out = CodedOutputStream.newInstance(os);

			Iterable<JobLogLine> lines = ((SimpleJobLogger) logger).getLogEntries();

			JobDataProtobuf.JobLogLine.Builder protobuf = JobDataProtobuf.JobLogLine.newBuilder();
			for (JobLogLine line : lines) {
				protobuf.setLevel(line.level);
				protobuf.setMessage(line.message);
				protobuf.setTimestamp(line.timestamp);
				protobuf.setType(line.type);
				if (line.exception != null) {
					mapToProtobuf(line.exception, protobuf.getExceptionBuilder());
				} else {
					protobuf.clearException();
				}

				JobDataProtobuf.JobLogLine message = protobuf.build();
				int size = message.getSerializedSize();
				out.writeRawVarint32(size);
				message.writeTo(out);
			}

			out.flush();

			okay = true;
		} finally {
			// Closeables.closeQuietly(out);
			Closeables.closeQuietly(os);

			if (!okay) {
				IoUtils.safeDelete(file);
			}
		}
	}

	private void mapToProtobuf(JobLogExceptionInfo exception,
			JobDataProtobuf.JobLogExceptionInfo.Builder exceptionBuilder) {
		exceptionBuilder.clear();
		exceptionBuilder.addAllInfo(exception.info);
	}

	private JobLogExceptionInfo mapFromProtobuf(JobDataProtobuf.JobLogExceptionInfo.Builder exceptionBuilder) {
		JobLogExceptionInfo exception = new JobLogExceptionInfo();
		exception.info = Lists.newArrayList();
		for (String info : exceptionBuilder.getInfoList()) {
			exception.info.add(info);
		}
		return exception;
	}

	private File toFile(Date endTime, PlatformLayerKey jobKey, String executionId) {
		long t = endTime.getTime();
		// Convert to seconds
		t /= 1000L;

		// Store in 1000 second buckets
		t /= 1000L;

		String dir = Long.toString(t);

		String fileName = executionId;

		File file = new File(baseDir, dir);
		if (!file.exists()) {
			file.mkdir();
		}
		file = new File(file, fileName);

		return file;
	}

}