package org.platformlayer.ops.pool;

import java.net.InetAddress;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.platformlayer.ops.networks.IpRange;

import com.google.common.collect.Iterables;

public class NetworkPoolBuilder extends PoolBuilderBase<InetAddress> {
	static final Logger log = Logger.getLogger(NetworkPoolBuilder.class);

	final IpRange range;
	final int skipCount;

	public NetworkPoolBuilder(String cidr, int skipCount) {
		this.range = IpRange.parse(cidr);
		this.skipCount = skipCount;
	}

	@Override
	protected Iterable<InetAddress> getItems() {
		return Iterables.skip(range.all(), skipCount);
	}

	@Override
	protected Properties buildProperties(InetAddress address) {
		Properties properties = new Properties();
		String cidr = address.getHostAddress() + "/" + range.getNetmaskLength();
		properties.setProperty("cidr", cidr);
		return properties;
	}

	@Override
	protected String toKey(InetAddress address) {
		return address.getHostAddress();
	}

}
