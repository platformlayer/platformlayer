package org.platformlayer.ops.uses;

import org.platformlayer.InetAddressChooser;
import org.platformlayer.core.model.PlatformLayerKey;

public interface LinkConsumer {

	PlatformLayerKey getKey();

	InetAddressChooser getInetAddressChooser();
}
