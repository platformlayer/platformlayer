package org.platformlayer.service.cloud.direct.ops;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.platformlayer.core.model.AddressModel;
import org.platformlayer.ops.networks.AddressModels;
import org.platformlayer.ops.pool.ResourcePool;
import org.platformlayer.ops.pool.TransformingResourcePool;

public class AssignPortToAddressPool extends TransformingResourcePool<AddressModel, InetSocketAddress> {

	private final int port;

	public AssignPortToAddressPool(ResourcePool<AddressModel> underlying, int port) {
		super(underlying);
		this.port = port;
	}

	@Override
	protected InetSocketAddress transform(AddressModel item) {
		InetAddress inetAddress = item.getInetAddress();
		return new InetSocketAddress(inetAddress, port);
	}

	@Override
	protected AddressModel reverse(InetSocketAddress item) {
		AddressModel addressModel = new AddressModel();
		addressModel.address = item.getAddress().getHostAddress();
		AddressModels.populateDefaults(addressModel);
		return addressModel;
	}
}
