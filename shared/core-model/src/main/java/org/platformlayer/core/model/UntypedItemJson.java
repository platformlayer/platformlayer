package org.platformlayer.core.model;

import org.platformlayer.common.Tagset;
import org.platformlayer.common.UntypedItem;

public class UntypedItemJson implements UntypedItem {

	@Override
	public PlatformLayerKey getKey() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Tagset getTags() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ManagedItemState getState() {
		throw new UnsupportedOperationException();
	}

}
