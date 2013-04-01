package org.platformlayer.ops.jobstore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import javax.inject.Singleton;

import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.jobs.model.JobLog;
import org.platformlayer.ops.log.JobLogger;

import com.fathomdb.io.IoUtils;
import com.google.common.io.Closeables;
import com.google.common.io.Files;

@Singleton
public class FilesystemJobLogStore extends JobLogStoreBase {
	final File baseDir;

	public FilesystemJobLogStore(File baseDir) {
		this.baseDir = baseDir;
	}

	@Override
	public JobLog getJobLog(Date startTime, PlatformLayerKey jobKey, String executionId, String cookie, int logSkip)
			throws IOException {
		File file = toFile(startTime, jobKey, executionId);
		if (!file.exists()) {
			return null;
		}

		return deserialize(Files.newInputStreamSupplier(file), logSkip);
	}

	@Override
	public String saveJobLog(PlatformLayerKey jobKey, String executionId, Date startTime, JobLogger logger)
			throws IOException {
		File file = toFile(startTime, jobKey, executionId);
		if (file.exists()) {
			throw new IllegalStateException("Job log file already exists");
		}

		OutputStream os = null;
		boolean okay = false;
		try {
			os = new FileOutputStream(file);

			serialize((SimpleJobLogger) logger, os);
			os.flush();
			okay = true;
		} finally {
			Closeables.closeQuietly(os);

			if (!okay) {
				IoUtils.safeDelete(file);
			}
		}

		// We don't use the cookie
		String cookie = null;
		return cookie;

	}

	private File toFile(Date startTime, PlatformLayerKey jobKey, String executionId) {
		long t = startTime.getTime();

		long tBucket = t;

		// Convert to seconds
		tBucket /= 1000L;

		// Store in 1000 second buckets
		tBucket /= 1000L;

		String dir = Long.toString(tBucket);

		String fileName = executionId;

		File file = new File(baseDir, dir);
		if (!file.exists()) {
			file.mkdir();
		}
		file = new File(file, fileName);

		return file;
	}

}