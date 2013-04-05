package org.platformlayer.ops.uses;

import java.util.Map;

import org.platformlayer.InetAddressChooser;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ops.OpsException;

public interface LinkTarget {
	Map<String, String> buildLinkTargetConfiguration(InetAddressChooser inetAddressChooser) throws OpsException;

	PlatformLayerKey getCaForClientKey();

	// Soon: void addChildren(OpsTreeBase consumer) throws OpsException;
}
