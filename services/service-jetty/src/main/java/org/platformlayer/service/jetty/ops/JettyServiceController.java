package org.platformlayer.service.jetty.ops;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.inject.Inject;

import org.platformlayer.InetAddressChooser;
import org.platformlayer.ops.Bound;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.Machine;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.helpers.InstanceHelpers;
import org.platformlayer.ops.http.HttpBackend;
import org.platformlayer.ops.instances.InstanceBuilder;
import org.platformlayer.ops.metrics.MetricsInstance;
import org.platformlayer.ops.networks.HasPorts;
import org.platformlayer.ops.networks.NetworkPoint;
import org.platformlayer.ops.networks.PublicEndpoint;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.jetty.model.JettyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.net.InetAddresses;

public class JettyServiceController extends OpsTreeBase implements HasPorts, HttpBackend {

	private static final Logger log = LoggerFactory.getLogger(JettyServiceController.class);

	public static final int PORT = 8080;

	@Bound
	JettyService model;

	@Inject
	InstanceHelpers instances;

	@Handler
	public void doOperation() {
	}

	@Override
	protected void addChildren() throws OpsException {
		String dnsName = model.dnsName;

		List<Integer> ports = getPorts();

		InstanceBuilder vm;
		{
			vm = InstanceBuilder.build(dnsName, this, model.getTags());
			vm.publicPorts.addAll(ports);
			// vm.hostPolicy.configureCluster(template.getPlacementKey());

			// TODO: This needs to be configurable (?)
			vm.minimumMemoryMb = 2048;

			addChild(vm);
		}

		vm.addChild(JettyInstall.class);

		vm.addChild(JettyInstance.class);

		for (int port : ports) {
			PublicEndpoint endpoint = injected(PublicEndpoint.class);
			// endpoint.network = null;
			endpoint.publicPort = port;
			endpoint.backendPort = port;
			endpoint.dnsName = dnsName;

			endpoint.tagItem = model.getKey();
			endpoint.parentItem = model.getKey();

			endpoint.transport = model.transport;

			vm.addChild(endpoint);
		}

		vm.addChild(MetricsInstance.class);
	}

	@Override
	public List<Integer> getPorts() {
		List<Integer> ports = Lists.newArrayList();
		ports.add(JettyServiceController.PORT);
		return ports;
	}

	@Override
	public URI getUri(NetworkPoint src) throws OpsException {
		int port = PORT;

		Machine machine = instances.getMachine(model);

		InetAddressChooser chooser = InetAddressChooser.preferIpv6();
		InetAddress address = machine.getBestAddress(src, port, chooser);

		String host = InetAddresses.toAddrString(address);

		URI uri;
		try {
			uri = new URI("http", null, host, port, null, null, null);
		} catch (URISyntaxException e) {
			throw new OpsException("Error building URI", e);
		}
		return uri;
	}

}
