package org.platformlayer.service.network.ops;

import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.ops.EnumUtils;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.firewall.Protocol;
import org.platformlayer.ops.firewall.Transport;
import org.platformlayer.ops.helpers.ProviderHelper;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.networks.HasPorts;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.network.model.NetworkConnection;

import com.google.common.collect.Lists;

public class NetworkConnectionController extends OpsTreeBase {
	static final Logger log = Logger.getLogger(NetworkConnectionController.class);

	@Inject
	OpsContext ops;

	@Inject
	ProviderHelper providers;

	@Inject
	PlatformLayerHelpers platformLayer;

	@Handler
	public void handler() {
	}

	@Override
	protected void addChildren() throws OpsException {
		NetworkConnection model = ops.getInstance(NetworkConnection.class);

		Protocol protocol = null;
		if (model.protocol != null) {
			protocol = EnumUtils.valueOfCaseInsensitive(Protocol.class, model.protocol);
		}

		Transport transport = null;
		// if (model.transport != null) {
		// protocol = EnumUtils.valueOfCaseInsensitive(Transport.class, model.transport);
		// }

		List<Integer> ports = Lists.newArrayList();
		if (model.port != 0) {
			ports.add(model.port);
			if (model.protocol == null) {
				protocol = Protocol.Tcp;
			}
		} else {
			ItemBase destItem = platformLayer.getItem(model.destItem);
			HasPorts hasPorts = providers.toInterface(destItem, HasPorts.class);

			ports.addAll(hasPorts.getPorts());

			if (model.protocol == null) {
				// TODO: Support UDP?
				protocol = Protocol.Tcp;
			}
		}

		for (int port : ports) {
			PlatformLayerFirewallEntry net = injected(PlatformLayerFirewallEntry.class);
			net.destItem = model.destItem;
			net.port = port;
			net.sourceItemKey = model.sourceItem;
			net.sourceCidr = model.sourceCidr;
			net.protocol = protocol;
			net.transport = transport;

			addChild(net);
		}
	}
}
