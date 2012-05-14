package org.platformlayer.ops.pool;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.platformlayer.ops.networks.AddressModel;

public class NetworkAddressPoolAssignment extends PoolAssignment<AddressModel> {
	private static final Logger log = Logger.getLogger(NetworkAddressPoolAssignment.class);

	@Override
	protected AddressModel map(Properties properties) {
		return AddressModel.build(properties);
	}
}
