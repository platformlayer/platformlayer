package org.platformlayer.ops.uses;

import org.platformlayer.InetAddressChooser;
import org.platformlayer.core.model.PlatformLayerKey;

public class SimpleLinkConsumer implements LinkConsumer {

	final PlatformLayerKey key;
	final InetAddressChooser inetAddressChooser;

	public SimpleLinkConsumer(PlatformLayerKey key, InetAddressChooser inetAddressChooser) {
		this.key = key;
		this.inetAddressChooser = inetAddressChooser;
	}

	@Override
	public PlatformLayerKey getKey() {
		return key;
	}

	@Override
	public InetAddressChooser getInetAddressChooser() {
		return inetAddressChooser;
	}
}
