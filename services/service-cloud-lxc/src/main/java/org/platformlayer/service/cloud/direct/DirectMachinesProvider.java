package org.platformlayer.service.cloud.direct;

import org.platformlayer.ops.ServiceProviderBase;
import org.platformlayer.xaas.Service;

@Service(DirectMachinesProvider.KEY)
public class DirectMachinesProvider extends ServiceProviderBase {
	public static final String KEY = "machines-direct";
}
