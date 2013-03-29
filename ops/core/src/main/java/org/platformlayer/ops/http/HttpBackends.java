package org.platformlayer.ops.http;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;

import javax.inject.Inject;

import org.platformlayer.InetAddressChooser;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.ops.Machine;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.helpers.InstanceHelpers;
import org.platformlayer.ops.networks.NetworkPoint;

import com.google.common.net.InetAddresses;

public class HttpBackends {

	@Inject
	InstanceHelpers instances;

	public URI buildUri(NetworkPoint src, String scheme, ItemBase model, int port) throws OpsException {
		Machine machine = instances.getMachine(model);

		InetAddressChooser chooser = InetAddressChooser.preferIpv6();
		InetAddress address = machine.getNetworkPoint().getBestAddress(src, chooser);

		String host = InetAddresses.toAddrString(address);

		URI uri;
		try {
			uri = new URI(scheme, null, host, port, null, null, null);
		} catch (URISyntaxException e) {
			throw new OpsException("Error building URI", e);
		}
		return uri;
	}

	public static HttpBackends get() {
		return OpsContext.injected(HttpBackends.class);
	}

}
