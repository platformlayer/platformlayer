package org.platformlayer.ops.tasks;

import org.platformlayer.TimeSpan;

public interface OperationQueue {
    void submit(OperationWorker operationWorker);

    void submit(OperationWorker operationWorker, TimeSpan delay);
}
