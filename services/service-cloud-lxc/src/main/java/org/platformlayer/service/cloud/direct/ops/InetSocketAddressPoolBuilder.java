package org.platformlayer.service.cloud.direct.ops;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.platformlayer.ops.pool.PoolBuilderBase;

public abstract class InetSocketAddressPoolBuilder extends PoolBuilderBase<InetSocketAddress> {
	static final Logger log = Logger.getLogger(InetSocketAddressPoolBuilder.class);

	@Override
	protected Properties buildProperties(InetSocketAddress socketAddress) {
		Properties properties = new Properties();
		properties.setProperty("port", String.valueOf(socketAddress.getPort()));
		InetAddress address = socketAddress.getAddress();
		if (!address.isAnyLocalAddress()) {
			properties.setProperty("address", address.getHostAddress());
		}
		return properties;
	}

}
