package org.platformlayer.ops.networks;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import org.platformlayer.core.model.AddressModel;

public class AddressModels {

	public static void populateDefaults(AddressModel model) {
		if (model.cidr != null) {
			IpRange ipRange = IpRange.parse(model.cidr);
			if (model.address == null) {
				model.address = ipRange.getAddress().getHostAddress();
			}
			if (model.netmask == null) {
				model.netmask = ipRange.getNetmask();
			}
			if (model.gateway == null) {
				model.gateway = ipRange.getGatewayAddress().getHostAddress();
			}

			// IpV4Range ipRange = IpV4Range.parse(address4, netmask4);
			// cidr4 = address4 + "/" + ipRange.getPrefixLength();
		}

		if (model.protocol == null) {
			InetAddress inetAddress;
			try {
				inetAddress = InetAddress.getByName(model.address);
			} catch (UnknownHostException e) {
				throw new IllegalStateException("Error resolving address: " + model.address, e);
			}
			if (inetAddress instanceof Inet4Address) {
				model.protocol = "inet";
			} else if (inetAddress instanceof Inet6Address) {
				model.protocol = "inet6";
			} else {
				throw new UnsupportedOperationException();
			}
		}
	}

	public static AddressModel build(Properties properties) {
		AddressModel model = new AddressModel();

		model.cidr = properties.getProperty("cidr");

		model.address = properties.getProperty("address");
		model.netmask = properties.getProperty("netmask");

		model.gateway = properties.getProperty("gateway");
		model.protocol = properties.getProperty("protocol");

		populateDefaults(model);

		return model;
	}
}
