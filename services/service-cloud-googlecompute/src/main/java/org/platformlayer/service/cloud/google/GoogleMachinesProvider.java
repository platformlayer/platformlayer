package org.platformlayer.service.cloud.google;

import org.platformlayer.ops.ServiceProviderBase;
import org.platformlayer.xaas.Service;

@Service(GoogleMachinesProvider.KEY)
public class GoogleMachinesProvider extends ServiceProviderBase {
	public static final String KEY = "machines-google";
}
