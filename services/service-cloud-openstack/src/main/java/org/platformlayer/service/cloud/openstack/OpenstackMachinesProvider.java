package org.platformlayer.service.cloud.openstack;

import org.platformlayer.ops.ServiceProviderBase;
import org.platformlayer.xaas.Service;

@Service(OpenstackMachinesProvider.KEY)
public class OpenstackMachinesProvider extends ServiceProviderBase {
	public static final String KEY = "machines-openstack";
}
