package org.platformlayer.service.platformlayer.ops.auth.system;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.platformlayer.InetAddressChooser;
import org.platformlayer.core.model.Tag;
import org.platformlayer.ops.Bound;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.instances.InstanceBuilder;
import org.platformlayer.ops.networks.PublicEndpoint;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.ops.uses.LinkTarget;
import org.platformlayer.service.platformlayer.model.SystemAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class SystemAuthServiceController extends OpsTreeBase implements LinkTarget {
	private static final Logger log = LoggerFactory.getLogger(SystemAuthServiceController.class);

	public static final int PORT = 35358;

	public static final String CERT_NAME = "clientcert.systemauth";

	@Bound
	SystemAuthService model;

	@Bound
	SystemAuthInstanceTemplate template;

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
			SystemAuthInstall install = vm.addChild(SystemAuthInstall.class);
		}

		{
			SystemAuthInstance service = vm.addChild(SystemAuthInstance.class);
		}

		{
			PublicEndpoint endpoint = vm.addChild(PublicEndpoint.class);
			// endpoint.network = null;
			endpoint.publicPort = port;
			endpoint.backendPort = port;
			endpoint.dnsName = dnsName;

			endpoint.tagItem = model.getKey();
			endpoint.parentItem = model.getKey();
		}
	}

	@Override
	public Map<String, String> buildLinkTargetConfiguration(InetAddressChooser inetAddressChooser) throws OpsException {
		Map<String, String> properties = Maps.newHashMap();

		List<String> systemAuthKeys = Lists.newArrayList();

		String systemAuthUrl = "https://" + model.dnsName + ":35358/";

		systemAuthKeys.addAll(Tag.PUBLIC_KEY_SIG.find(model));
		Collections.sort(systemAuthKeys); // Keep it stable

		properties.put("ssl.keys", Joiner.on(',').join(systemAuthKeys));
		properties.put("url", systemAuthUrl);
		properties.put("ssl.cert", CERT_NAME);

		return properties;
	}
}
