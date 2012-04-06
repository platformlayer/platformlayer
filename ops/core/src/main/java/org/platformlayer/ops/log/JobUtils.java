package org.platformlayer.ops.log;

import java.util.Arrays;

import org.platformlayer.jobs.model.JobLogExceptionInfo;

public class JobUtils {

    public static JobLogExceptionInfo buildJobLogExceptionInfo(String[] exceptionInfo) {
        JobLogExceptionInfo exception = new JobLogExceptionInfo();
        exception.getInfo().addAll(Arrays.asList(exceptionInfo));
        return exception;
    }

}
