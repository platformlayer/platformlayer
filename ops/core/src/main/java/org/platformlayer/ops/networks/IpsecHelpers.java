package org.platformlayer.ops.networks;

import javax.inject.Inject;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.ManagedItemState;
import org.platformlayer.core.model.Secret;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.helpers.ProviderHelper;
import org.platformlayer.ops.helpers.ProviderHelper.ProviderOf;

public class IpsecHelpers {
	@Inject
	ProviderHelper providerHelper;

	public Secret getIpsecSecret() throws OpsException {
		Secret secret = null;

		for (ProviderOf<HasIpsecPolicy> ipsecPolicyProvider : providerHelper.listItemsProviding(HasIpsecPolicy.class)) {
			ItemBase item = ipsecPolicyProvider.getItem();

			if (item.getState() != ManagedItemState.ACTIVE) {
				continue;
			}

			HasIpsecPolicy ipsec = ipsecPolicyProvider.get();

			if (secret != null) {
				throw new IllegalStateException("Multiple IPSEC policies found");
			}

			secret = ipsec.getIpsecPreSharedKey(item);
			if (secret == null) {
				// Should we allow this?
				throw new IllegalStateException();
			}
		}

		if (secret == null) {
			throw new OpsException("Ipsec policy not found");
		}

		return secret;
	}

}
