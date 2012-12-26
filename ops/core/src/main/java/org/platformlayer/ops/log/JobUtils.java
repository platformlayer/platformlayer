package org.platformlayer.ops.log;

import java.util.Arrays;
import java.util.List;

import org.platformlayer.jobs.model.JobLogExceptionInfo;

import com.google.common.collect.Lists;

public class JobUtils {

	public static JobLogExceptionInfo buildJobLogExceptionInfo(List<String[]> exceptionStacks) {
		if (exceptionStacks == null || exceptionStacks.isEmpty()) {
			return null;
		}

		JobLogExceptionInfo ret = null;

		for (String[] exceptionStack : Lists.reverse(exceptionStacks)) {
			JobLogExceptionInfo exception = new JobLogExceptionInfo();
			exception.getInfo().addAll(Arrays.asList(exceptionStack));
			exception.inner = ret;
			ret = exception;
		}

		return ret;
	}

}
