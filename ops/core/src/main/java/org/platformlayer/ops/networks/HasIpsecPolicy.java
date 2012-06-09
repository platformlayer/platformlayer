package org.platformlayer.ops.networks;

import org.platformlayer.core.model.Secret;

public interface HasIpsecPolicy {
	Secret getIpsecPreSharedKey(Object model);
}
