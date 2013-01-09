package org.platformlayer.service.cloud.direct.ops;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Properties;

import org.platformlayer.ops.pool.PoolBuilderBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class InetSocketAddressPoolBuilder extends PoolBuilderBase<InetSocketAddress> {
	private static final Logger log = LoggerFactory.getLogger(InetSocketAddressPoolBuilder.class);

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

	@Override
	protected String toKey(InetSocketAddress item) {
		String s = item.getAddress().getHostAddress() + ":" + item.getPort();
		return s;
	}

}
