package org.platformlayer.service.cloud.direct.ops;

import java.io.File;
import java.io.IOException;

import org.platformlayer.ops.Bound;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.CommandEnvironment;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.filesystem.TemplatedFile;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkBridge extends OpsTreeBase {

	private static final Logger log = LoggerFactory.getLogger(NetworkBridge.class);

	@Bound
	HostTemplate template;

	// public IpRange ipRange;
	// public static NetworkBridge build(String bridge, IpRange ipRange) {
	// NetworkBridge conf = new NetworkBridge();
	// conf.bridge = bridge;
	// conf.ipRange = ipRange;
	// return conf;
	// }

	@Handler
	public void handler() throws OpsException, IOException {
		// We assume that the bridge has been manually configured like this:

		// in /etc/network/interfaces
		// auto br100
		// iface br100 inet static
		// bridge_ports none
		// bridge_stp off
		// bridge_maxwait 0
		// bridge_fd 0
		// address 172.16.0.1
		// netmask 255.240.0.0
		// up ip addr add <ipv6>::1/64 dev br100

		// If we're using an ipv6 tunnel, we probably also need something like this:
		// /etc/network/if-up.d/ip6tables-forward-tunnel
		// #!/bin/bash
		//
		// set -e
		//
		// ip6tables -A FORWARD -i tun6rd -o br100 -j ACCEPT
		// ip6tables -A FORWARD -i br100 -o tun6rd -j ACCEPT
		//
		// iptables -t filter -I INPUT -p 41 -j ACCEPT
		// iptables -t filter -I OUTPUT -p 41 -j ACCEPT
		//
		// exit 0

	}

	@Override
	protected void addChildren() throws OpsException {
		// We would need proxy_arp if they were on the same IPV4 network

		// Do we need proxy_ndp ???
		// echo 1 > /proc/sys/net/ipv6/conf/eth0/proxy_ndp

		addChild(SysctlSetting.build("net.ipv4.ip_forward", "1"));
		addChild(SysctlSetting.build("net.ipv6.conf.all.forwarding", "1"));

		{
			File scriptPath = new File("/etc/network/if-up.d/nat-for-bridge");
			TemplatedFile nat = addChild(TemplatedFile.build(template, scriptPath));
			nat.setFileMode("0755");

			// Simulate an ifup run
			Command command = Command.build(scriptPath);
			CommandEnvironment env = new CommandEnvironment();
			env.put("IFACE", template.getPublicInterface());
			env.put("MODE", "start");
			env.put("ADDRFAM", "inet");
			command.setEnvironment(env);

			nat.setUpdateAction(command);
		}

	}
}
