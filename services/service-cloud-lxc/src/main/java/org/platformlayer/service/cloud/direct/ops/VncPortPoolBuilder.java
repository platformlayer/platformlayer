package org.platformlayer.service.cloud.direct.ops;

import java.net.InetSocketAddress;
import java.util.List;

import com.google.common.collect.Lists;

public class VncPortPoolBuilder extends InetSocketAddressPoolBuilder {

	@Override
	protected Iterable<InetSocketAddress> getItems() {
		List<InetSocketAddress> sockets = Lists.newArrayList();
		for (int port = 5900; port <= 5999; port++) {
			sockets.add(new InetSocketAddress(port));
		}
		return sockets;
	}
}
