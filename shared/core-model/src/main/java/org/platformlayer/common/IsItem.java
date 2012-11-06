package org.platformlayer.common;

import org.platformlayer.core.model.ManagedItemState;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Tags;

public interface IsItem {
	PlatformLayerKey getKey();

	Tags getTags();

	ManagedItemState getState();
}
