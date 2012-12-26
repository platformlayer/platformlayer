package org.platformlayer.ops.log;

import java.util.List;

public interface JobLogger {

	void logMessage(String message, List<String[]> exceptionStacks, int levelInt);

	void enterScope(Object controller);

	void exitScope();

}
