package org.platformlayer.ops.log;


public interface JobLogger {

	void logMessage(String message, String[] exceptionInfo, int levelInt);

}
