package org.platformlayer.ops.schedule;

import java.io.Closeable;
import java.util.TimerTask;

import org.platformlayer.TimeSpan;

import com.google.inject.ImplementedBy;

/**
 * Class implements thread management, in particular for web apps so we can shutdown background threads and stop permgen
 * problems
 * 
 * @author justinsb
 * 
 */
@ImplementedBy(SimpleThreadServices.class)
public interface ThreadServices extends Closeable {
	void schedule(String description, TimerTask task, TimeSpan delay, TimeSpan interval);

	void scheduleOneOff(String description, TimerTask task, TimeSpan delay);

	// ExecutorService getExecutorService();

}