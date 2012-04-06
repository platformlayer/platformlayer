package org.platformlayer.ops.metrics.collectd;

import org.platformlayer.core.model.PlatformLayerKey;

public class CollectdHelpers {

    public static String toCollectdKey(PlatformLayerKey modelKey) {
        // TODO: Multiple machines per service
        String serviceKey = modelKey.getServiceType().getKey();
        String itemType = modelKey.getItemType().getKey();
        String key = modelKey.getItemId().getKey() + "." + itemType + "." + serviceKey;
        return key;
    }

}
