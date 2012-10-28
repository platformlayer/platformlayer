package org.platformlayer.common;

import org.platformlayer.core.model.ManagedItemState;
import org.platformlayer.core.model.PlatformLayerKey;

public interface IsItem {
	PlatformLayerKey getKey();

	Tagset getTags();

	ManagedItemState getState();
}
