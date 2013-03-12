package org.platformlayer.service.platformlayer.ops.backend;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;

import javax.inject.Inject;

import org.platformlayer.EnumUtils;
import org.platformlayer.InetAddressChooser;
import org.platformlayer.ops.Bound;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.Machine;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.firewall.Transport;
import org.platformlayer.ops.helpers.InstanceHelpers;
import org.platformlayer.ops.http.HttpBackend;
import org.platformlayer.ops.http.HttpManager;
import org.platformlayer.ops.http.HttpManager.SslMode;
import org.platformlayer.ops.instances.InstanceBuilder;
import org.platformlayer.ops.networks.NetworkPoint;
import org.platformlayer.ops.networks.PublicEndpoint;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.platformlayer.model.PlatformLayerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.net.InetAddresses;

public class PlatformLayerServiceController extends OpsTreeBase implements HttpBackend {
	private static final Logger log = LoggerFactory.getLogger(PlatformLayerServiceController.class);

	public static final int PORT = 8082;

	@Bound
	PlatformLayerInstanceTemplate template;

	@Bound
	PlatformLayerService model;

	@Inject
	HttpManager loadBalancing;

	@Inject
	InstanceHelpers instances;

	@Handler
	public void handler() {
	}

	@Override
	protected void addChildren() throws OpsException {
		int port = PORT;

		String dnsName = model.dnsName;

		InstanceBuilder vm;
		{
			vm = InstanceBuilder.build(dnsName, this, model.getTags());
			vm.publicPorts.add(port);
			vm.hostPolicy.configureCluster(template.getPlacementKey());

			// TODO: This needs to be configurable (?)
			vm.minimumMemoryMb = 2048;

			addChild(vm);
		}

		{
			PlatformLayerInstall install = injected(PlatformLayerInstall.class);
			vm.addChild(install);
		}

		{
			PlatformLayerInstance service = injected(PlatformLayerInstance.class);
			vm.addChild(service);
		}

		{
			PublicEndpoint endpoint = injected(PublicEndpoint.class);
			// endpoint.network = null;
			endpoint.publicPort = port;
			endpoint.backendPort = port;

			endpoint.tagItem = model.getKey();
			endpoint.parentItem = model.getKey();

			if (model.transport != null) {
				endpoint.transport = EnumUtils.valueOfCaseInsensitive(Transport.class, model.transport);
			} else {
				endpoint.transport = Transport.Ipv6;
			}

			vm.addChild(endpoint);
		}

		loadBalancing.addHttpSite(this, model, model.dnsName, template.getSslKeyPath(), SslMode.Terminate);
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
			uri = new URI("https", null, host, port, null, null, null);
		} catch (URISyntaxException e) {
			throw new OpsException("Error building URI", e);
		}
		return uri;
	}

}
