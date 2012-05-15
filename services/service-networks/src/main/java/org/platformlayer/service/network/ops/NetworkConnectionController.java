package org.platformlayer.service.network.ops;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.ops.EnumUtils;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.firewall.FirewallRecord.Protocol;
import org.platformlayer.ops.firewall.FirewallRecord.Transport;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.network.model.NetworkConnection;

public class NetworkConnectionController extends OpsTreeBase {
	static final Logger log = Logger.getLogger(NetworkConnectionController.class);

	@Inject
	OpsContext ops;

	@Handler
	public void handler() {
	}

	@Override
	protected void addChildren() throws OpsException {
		NetworkConnection model = ops.getInstance(NetworkConnection.class);

		Protocol protocol = Protocol.Tcp;
		if (model.protocol != null) {
			protocol = EnumUtils.valueOfCaseInsensitive(Protocol.class, model.protocol);
		}

		Transport transport = null;
		// if (model.transport != null) {
		// protocol = EnumUtils.valueOfCaseInsensitive(Transport.class, model.transport);
		// }

		{
			PlatformLayerFirewallEntry net = injected(PlatformLayerFirewallEntry.class);
			net.destItem = model.destItem;
			net.port = model.port;
			net.sourceItemKey = model.sourceItem;
			net.sourceCidr = model.sourceCidr;
			net.protocol = protocol;
			net.transport = transport;

			addChild(net);
		}
	}
}
