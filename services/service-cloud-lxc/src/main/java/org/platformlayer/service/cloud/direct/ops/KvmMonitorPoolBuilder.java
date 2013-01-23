package org.platformlayer.service.cloud.direct.ops;

import java.net.InetSocketAddress;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class KvmMonitorPoolBuilder extends InetSocketAddressPoolBuilder {

	private static final Logger log = LoggerFactory.getLogger(KvmMonitorPoolBuilder.class);

	@Override
	public Iterable<String> getItems() {
		List<String> sockets = Lists.newArrayList();
		for (int port = 5500; port <= 5599; port++) {
			sockets.add("" + port);
		}
		return sockets;
	}

	@Override
	public String toKey(InetSocketAddress item) {
		String s = "" + item.getPort();
		return s;
	}

	@Override
	public InetSocketAddress toItem(String key) {
		int port = Integer.parseInt(key);
		return new InetSocketAddress(port);
	}

}
