package org.platformlayer.service.zookeeper;

import javax.inject.Inject;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.ServiceProviderBase;
import org.platformlayer.ops.crypto.Passwords;
import org.platformlayer.service.zookeeper.model.ZookeeperCluster;
import org.platformlayer.xaas.Service;

@Service("zookeeper")
public class ZookeeperServiceProvider extends ServiceProviderBase {
	@Inject
	Passwords passwords;

	@Override
	public void beforeCreateItem(ItemBase item) throws OpsException {
		if (item instanceof ZookeeperCluster) {
			ZookeeperCluster model = (ZookeeperCluster) item;
			// if (Secret.isNullOrEmpty(model.ipsecSecret)) {
			// model.ipsecSecret = passwords.generateIpsecPSK();
			// }
		}

		super.beforeCreateItem(item);
	}

}
