package org.platformlayer.ops.uses;

import java.util.Map;

import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ops.OpsException;

public interface LinkTarget {
	Map<String, String> buildLinkTargetConfiguration(LinkConsumer consumer) throws OpsException;

	PlatformLayerKey getCaForClientKey();

	// Soon: void addChildren(OpsTreeBase consumer) throws OpsException;
}
