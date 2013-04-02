package org.platformlayer.ops.jobstore;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.platformlayer.jobs.model.JobLog;
import org.platformlayer.jobs.model.JobLogExceptionInfo;
import org.platformlayer.jobs.model.JobLogLine;
import org.platformlayer.ops.jobstore.protobuf.JobDataProtobuf;

import com.google.common.collect.Lists;
import com.google.common.io.Closeables;
import com.google.common.io.InputSupplier;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;

public abstract class JobLogStoreBase implements JobLogStore {
	protected JobLog deserialize(InputSupplier<? extends InputStream> iss, int logSkip) throws IOException {
		ArrayList<JobLogLine> lines = Lists.newArrayList();

		InputStream is = null;
		CodedInputStream in = null;
		try {
			is = iss.getInput();
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

	protected void serialize(SimpleJobLogger logger, OutputStream os) throws IOException {
		GZIPOutputStream gzip = new GZIPOutputStream(os);
		CodedOutputStream out = CodedOutputStream.newInstance(gzip);

		Iterable<JobLogLine> lines = logger.getLogEntries();

		JobDataProtobuf.JobLogLine.Builder protobuf = JobDataProtobuf.JobLogLine.newBuilder();
		for (JobLogLine line : lines) {
			protobuf.setLevel(line.level);
			if (line.message != null) {
				protobuf.setMessage(line.message);
			} else {
				protobuf.clearMessage();
			}
			protobuf.setTimestamp(line.timestamp);
			if (line.type != null) {
				protobuf.setType(line.type);
			} else {
				protobuf.clearType();
			}
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
		gzip.finish();
	}

	private void mapToProtobuf(JobLogExceptionInfo exception,
			JobDataProtobuf.JobLogExceptionInfo.Builder exceptionBuilder) {
		exceptionBuilder.clear();
		exceptionBuilder.addAllInfo(exception.info);
		if (exception.inner != null) {
			mapToProtobuf(exception.inner, exceptionBuilder.getInnerBuilder());
		}
	}

	private JobLogExceptionInfo mapFromProtobuf(JobDataProtobuf.JobLogExceptionInfo.Builder exceptionBuilder) {
		JobLogExceptionInfo exception = new JobLogExceptionInfo();
		exception.info = Lists.newArrayList();
		for (String info : exceptionBuilder.getInfoList()) {
			exception.info.add(info);
		}

		if (exceptionBuilder.hasInner()) {
			exception.inner = mapFromProtobuf(exceptionBuilder.getInnerBuilder());
		}

		return exception;
	}
}