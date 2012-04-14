package org.platformlayer.ops.tasks;

import org.platformlayer.TimeSpan;

public interface OperationQueue {
	void submit(OperationWorker operationWorker);

	void submitRetry(OperationWorker operationWorker, TimeSpan delay);
}
