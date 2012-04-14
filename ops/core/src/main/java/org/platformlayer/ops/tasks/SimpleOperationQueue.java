package org.platformlayer.ops.tasks;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

import org.platformlayer.TimeSpan;

public class SimpleOperationQueue implements OperationQueue {
    @Inject
    ExecutorService executorService;

    final Timer timer = new Timer();;

    @Override
    public void submit(OperationWorker operationWorker) {
        executorService.submit(operationWorker);
    }

    @Override
    public void submitRetry(final OperationWorker operationWorker, TimeSpan delay) {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                executorService.submit(operationWorker);
            }
        }, delay.getTotalMilliseconds());
    }

}
