package org.platformlayer.service.dnsresolver.ops;

import java.net.InetAddress;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.platformlayer.ops.Bound;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.Machine;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.dns.DnsResolverProvider;
import org.platformlayer.ops.helpers.InstanceHelpers;
import org.platformlayer.ops.instances.InstanceBuilder;
import org.platformlayer.ops.metrics.MetricsInstance;
import org.platformlayer.ops.networks.NetworkPoint;
import org.platformlayer.ops.packages.PackageDependency;
import org.platformlayer.ops.service.ManagedService;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.dnsresolver.model.DnsResolverService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

public class DnsResolverServiceController extends OpsTreeBase implements DnsResolverProvider {
	private static final Logger log = LoggerFactory.getLogger(DnsResolverServiceController.class);

	@Bound
	DnsResolverService model;

	@Inject
	InstanceHelpers instances;

	@Handler
	public void doOperation() {
	}

	@Override
	protected void addChildren() throws OpsException {
		if (Strings.isNullOrEmpty(model.dnsName)) {
			throw new IllegalArgumentException("dnsName must be specified");
		}

		InstanceBuilder instance = InstanceBuilder.build(model.dnsName, this);
		addChild(instance);

		instance.addChild(PackageDependency.build("bind9"));
		instance.addChild(ManagedService.build("bind9"));

		instance.addChild(MetricsInstance.class);

		// Debian bind9 sets up a recursive resolver by default :-)

		// TODO: Monit

		// TODO: Configure /etc/resolv.conf on servers
		// TODO: Refresh all our servers so that they use this resolver??

	}

	@Override
	public List<InetAddress> findAddresses(NetworkPoint from) throws OpsException {
		Machine machine = instances.getMachine(model);
		if (machine == null) {
			return Collections.emptyList();
		}

		List<InetAddress> addresses = machine.findAddresses(from, 53);
		return addresses;
	}
}
