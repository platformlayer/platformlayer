package org.platformlayer.ops;

import org.platformlayer.auth.OpsProject;
import org.platformlayer.core.model.PlatformLayerKey;

public interface MultitenantConfiguration {

	OpsProject getMasterProject();

	Iterable<PlatformLayerKey> getMappedItems();

}
