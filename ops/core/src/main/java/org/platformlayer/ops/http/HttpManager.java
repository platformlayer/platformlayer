package org.platformlayer.ops.http;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.ops.tree.OwnedItem;

public interface HttpManager {
	OwnedItem<?> addHttpSite(OpsTreeBase parent, ItemBase model, String dnsName, PlatformLayerKey sslKey)
			throws OpsException;
}
