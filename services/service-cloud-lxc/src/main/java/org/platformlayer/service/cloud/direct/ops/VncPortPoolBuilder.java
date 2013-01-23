package org.platformlayer.service.cloud.direct.ops;

import java.net.InetSocketAddress;
import java.util.List;

import com.google.common.collect.Lists;

public class VncPortPoolBuilder extends InetSocketAddressPoolBuilder {

	@Override
	public Iterable<String> getItems() {
		List<String> sockets = Lists.newArrayList();
		for (int port = 5900; port <= 5999; port++) {
			sockets.add("" + port);
		}
		return sockets;
	}

	@Override
	public String toKey(InetSocketAddress item) {
		return String.valueOf(item.getPort());
	}

	@Override
	public InetSocketAddress toItem(String key) {
		int port = Integer.parseInt(key);
		return new InetSocketAddress(port);
	}
}
