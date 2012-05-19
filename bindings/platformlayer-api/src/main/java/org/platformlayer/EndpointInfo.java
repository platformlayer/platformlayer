package org.platformlayer;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.platformlayer.core.model.Tag;
import org.platformlayer.core.model.Tags;

import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.net.InetAddresses;

public class EndpointInfo {

	public EndpointInfo() {
	}

	public EndpointInfo(String address, int port) {
		this.publicIp = address;
		this.port = port;
	}

	public EndpointInfo(InetAddress address, int port) {
		this(address.getHostAddress(), port);
	}

	public EndpointInfo(InetSocketAddress socketAddress) {
		this(socketAddress.getAddress(), socketAddress.getPort());
	}

	public static List<EndpointInfo> getEndpoints(Tags tags) {
		List<EndpointInfo> endpoints = Lists.newArrayList();

		for (String publicEndpoint : tags.find(Tag.PUBLIC_ENDPOINT)) {
			ArrayList<String> components = Lists.newArrayList(Splitter.on(":").split(publicEndpoint));
			if (components.size() == 2) {
				EndpointInfo info = new EndpointInfo();
				info.publicIp = components.get(0);
				info.port = Integer.parseInt(components.get(1));

				endpoints.add(info);
			} else {
				throw new IllegalStateException();
			}
		}
		return endpoints;

	}

	public static List<EndpointInfo> findEndpoints(Tags tags, Integer port) {
		List<EndpointInfo> endpoints = Lists.newArrayList();

		for (EndpointInfo publicEndpoint : getEndpoints(tags)) {
			if (publicEndpoint.matches(port)) {
				endpoints.add(publicEndpoint);
			}
		}
		return endpoints;
	}

	public String publicIp;
	public Integer port;

	public boolean matches(Integer port) {
		if (this.port == null || port == null || port == 0) {
			return true;
		}
		if (!Objects.equal(port, this.port)) {
			return false;
		}
		return true;
	}

	public Tag toTag() {
		return new Tag(Tag.PUBLIC_ENDPOINT, publicIp + ":" + port);
	}

	@Override
	public String toString() {
		return publicIp + ":" + port;
	}

	public InetAddress getAddress() {
		return InetAddresses.forString(publicIp);
	}

}
