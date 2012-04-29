package org.platformlayer.tests;

import java.io.IOException;
import java.util.EnumSet;

import org.platformlayer.TimeSpan;
import org.platformlayer.TypedPlatformLayerClient;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.ManagedItemState;
import org.platformlayer.ops.OpsException;

public class ManagedItemStatePoller<T extends ItemBase> {
	final TypedPlatformLayerClient client;

	T item;
	EnumSet<ManagedItemState> transitionalStates;
	EnumSet<ManagedItemState> exitStates;
	TimeSpan timeout;

	public ManagedItemStatePoller(TypedPlatformLayerClient client) {
		this.client = client;
	}

	public <T extends ItemBase> T waitForState() throws OpsException, IOException {
		Class<T> itemClass = (Class<T>) item.getClass();

		if (transitionalStates == null) {
			transitionalStates.complementOf(exitStates);
		}

		long startedAt = System.currentTimeMillis();

		while (true) {
			if (timeout != null && timeout.hasTimedOut(startedAt)) {
				throw new IllegalStateException("Timed out");
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				throw new IllegalStateException("Interrupted", e);
			}

			T latest = client.getItem(item.getKey(), itemClass);
			ManagedItemState state = latest.getState();
			if (exitStates.contains(state)) {
				System.out.println("Exiting; state=" + state);
				return latest;
			}

			if (transitionalStates.contains(state)) {
				System.out.println("Continuing to wait for " + item.getKey() + "; state=" + state);
			} else {
				throw new IllegalStateException("Unexpected state: " + state + " for " + latest);
			}
		}
	}
}
