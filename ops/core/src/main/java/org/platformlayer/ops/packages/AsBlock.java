package org.platformlayer.ops.packages;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.List;

import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.SshOpsTarget;
import org.platformlayer.ops.networks.IpRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.net.InetAddresses;

public class AsBlock {
	private static final Logger log = LoggerFactory.getLogger(AsBlock.class);

	final String key;
	final Country country;

	public final List<IpRange> announcements = Lists.newArrayList();

	public AsBlock(String key, Country country) {
		this.key = key;
		this.country = country;
	}

	public static final AsBlock HETZNER;
	public static final AsBlock SOFTLAYER;
	public static final AsBlock GOOGLE_COMPUTE_ENGINE;

	static final List<AsBlock> ALL = Lists.newArrayList();

	static {
		HETZNER = addAsBlock("AS24940", Country.DE);
		HETZNER.add("5.9.0.0/16");
		// HETZNER.add("46.4.0.0/16");
		// HETZNER.add("78.46.0.0/15");
		// HETZNER.add("85.10.192.0/18");
		// HETZNER.add("88.198.0.0/16");
		// HETZNER.add("91.220.49.0/24");
		// HETZNER.add("176.9.0.0/16");
		// HETZNER.add("176.102.168.0/21");
		// HETZNER.add("178.63.0.0/16");
		// HETZNER.add("188.40.0.0/16");
		// HETZNER.add("193.110.6.0/23");
		// HETZNER.add("193.223.77.0/24");
		// HETZNER.add("194.42.180.0/22");
		// HETZNER.add("194.42.184.0/22");
		// HETZNER.add("213.133.96.0/19");
		// HETZNER.add("213.239.192.0/18");

		HETZNER.add("2a01:04f8::/32");

		{
			AsBlock as = addAsBlock("AS13354", Country.US);
			as.add("209.105.224.0/19");
			// as.add("2607:5500::/32");
			// as.add("2607:f348::/32");
		}

		{
			AsBlock as = addAsBlock("AS36351", Country.US);
			as.add("184.173.128.0/18");
			as.add("2607:f0d0::/32");
			SOFTLAYER = as;
		}

		{
			AsBlock as = addAsBlock("AS15169", Country.US);

			// Google advertises a large number of prefixes under this block
			// including blocks under different countries...
			// We probably have to rethink our maping here (each netblock should have its own country)?

			as.add("173.255.112.0/20");
			as.add("108.59.80.0/20");
			GOOGLE_COMPUTE_ENGINE = as;
		}
	}

	public void add(String ipRange) {
		announcements.add(IpRange.parse(ipRange));
	}

	private static AsBlock addAsBlock(String key, Country country) {
		AsBlock block = new AsBlock(key, country);

		ALL.add(block);
		return block;
	}

	public Country getCountry() {
		return country;
	}

	public static AsBlock find(InetAddress address) {
		if (address instanceof Inet6Address) {
			Inet6Address ipv6 = (Inet6Address) address;
			if (InetAddresses.is6to4Address(ipv6)) {
				InetAddress ipv4 = InetAddresses.get6to4IPv4Address(ipv6);
				return find(ipv4);
			}
		}

		for (AsBlock asBlock : ALL) {
			if (asBlock.contains(address)) {
				return asBlock;
			}
		}
		return null;
	}

	private boolean contains(InetAddress address) {
		for (IpRange announcement : announcements) {
			if (announcement.isInRange(address)) {
				return true;
			}
		}

		return false;
	}

	public static AsBlock find(OpsTarget target) {
		SshOpsTarget sshOpsTarget = (SshOpsTarget) target;
		InetAddress host = sshOpsTarget.getHost();

		AsBlock asBlock = find(host);

		if (asBlock == null) {
			log.warn("Could not determine AS-Block for: " + host.getHostAddress() + " (" + target + ")");
		}

		return asBlock;
	}

}