package org.platformlayer.ops.pool;

import java.util.Properties;

import org.slf4j.*;
import org.platformlayer.ops.networks.AddressModel;

public class NetworkAddressPoolAssignment extends PoolAssignment<AddressModel> {
	private static final Logger log = LoggerFactory.getLogger(NetworkAddressPoolAssignment.class);

	@Override
	protected AddressModel map(Properties properties) {
		return AddressModel.build(properties);
	}
}
