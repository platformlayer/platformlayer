package org.platformlayer.service.cloud.google.ops.compute;

import com.google.api.services.compute.model.Instance;

/**
 * Attempts to abstract away the raw instance state values
 * 
 * @author justinsb
 * 
 */
public class InstanceState {
	private final String key;

	public static InstanceState get(String key) {
		return new InstanceState(key);
	}

	public static InstanceState get(Instance instance) {
		return get(instance.getStatus());
	}

	private InstanceState(String key) {
		this.key = key;

	}

	@Override
	public String toString() {
		return key;
	}

	public boolean isStarting() {
		return key.equals("PROVISIONING") || key.equals("STAGING");
	}

	public boolean isRunning() {
		return key.equals("RUNNING");
	}

	// public boolean isTerminated() {
	// // TODO: Is this the right state?
	// return key.equals("DELETED");
	// }
	//
	// public boolean isTerminating() {
	// // TODO: Is this the right state?
	// return key.equals("DELETING");
	// }

}
