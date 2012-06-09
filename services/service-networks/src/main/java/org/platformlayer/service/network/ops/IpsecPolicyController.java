package org.platformlayer.service.network.ops;

import org.platformlayer.core.model.Secret;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.networks.HasIpsecPolicy;
import org.platformlayer.service.network.model.IpsecPolicy;

public class IpsecPolicyController implements HasIpsecPolicy {
	@Handler
	public void handler() {
	}

	@Override
	public Secret getIpsecPreSharedKey(Object model) {
		// TODO: Have controller per model; inject item? Or use OpsContext?
		return ((IpsecPolicy) model).ipsecSecret;
	}
}
