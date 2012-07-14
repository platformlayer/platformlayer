package org.platformlayer.ops;

import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.model.ProjectAuthorization;

public interface MultitenantConfiguration {

	ProjectAuthorization getMasterProject();

	Iterable<PlatformLayerKey> getMappedItems();

}
