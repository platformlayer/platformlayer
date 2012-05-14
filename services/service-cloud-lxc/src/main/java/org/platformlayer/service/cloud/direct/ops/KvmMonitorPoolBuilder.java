package org.platformlayer.service.cloud.direct.ops;

import java.net.InetSocketAddress;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.common.collect.Lists;

public class KvmMonitorPoolBuilder extends InetSocketAddressPoolBuilder {
	static final Logger log = Logger.getLogger(KvmMonitorPoolBuilder.class);

	@Override
	protected Iterable<InetSocketAddress> getItems() {
		List<InetSocketAddress> sockets = Lists.newArrayList();
		for (int port = 5500; port <= 5599; port++) {
			sockets.add(new InetSocketAddress(port));
		}
		return sockets;
	}

}
