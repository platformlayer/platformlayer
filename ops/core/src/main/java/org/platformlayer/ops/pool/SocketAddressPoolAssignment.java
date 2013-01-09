package org.platformlayer.ops.pool;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Properties;

import org.slf4j.*;
import org.platformlayer.ops.networks.AddressModel;

import com.google.common.net.InetAddresses;

public class SocketAddressPoolAssignment extends PoolAssignment<InetSocketAddress> {
	private static final Logger log = LoggerFactory.getLogger(SocketAddressPoolAssignment.class);

	@Override
	protected InetSocketAddress map(Properties properties) {
		AddressModel addressModel = AddressModel.build(properties);

		int port = Integer.parseInt(properties.getProperty("port"));

		String addressString = addressModel.getAddress();

		if (addressString == null) {
			return new InetSocketAddress(port);
		} else {
			InetAddress address;
			try {
				address = InetAddresses.forString(addressString);
			} catch (IllegalArgumentException e) {
				throw new IllegalStateException("Error mapping address: " + addressString);
			}
			return new InetSocketAddress(address, port);
		}

	}
}
