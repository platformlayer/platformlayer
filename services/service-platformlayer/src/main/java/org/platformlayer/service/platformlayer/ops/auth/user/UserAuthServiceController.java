package org.platformlayer.service.platformlayer.ops.auth.user;

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
import org.platformlayer.service.platformlayer.model.UserAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class UserAuthServiceController extends OpsTreeBase implements LinkTarget {

	private static final Logger log = LoggerFactory.getLogger(UserAuthServiceController.class);

	public static final int PORT = 5001;

	@Bound
	UserAuthInstanceTemplate template;

	@Bound
	UserAuthService model;

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
			UserAuthInstall install = vm.addChild(UserAuthInstall.class);
		}

		{
			UserAuthInstance service = vm.addChild(UserAuthInstance.class);
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

		List<String> userAuthKeys = Lists.newArrayList();

		String baseUrl = "https://" + model.dnsName + ":5001/";

		userAuthKeys.addAll(Tag.PUBLIC_KEY_SIG.find(model));
		Collections.sort(userAuthKeys); // Keep it stable

		properties.put("ssl.keys", Joiner.on(',').join(userAuthKeys));
		properties.put("url", baseUrl);

		// The ssl cert is actually multitenant.cert

		return properties;
	}
}
