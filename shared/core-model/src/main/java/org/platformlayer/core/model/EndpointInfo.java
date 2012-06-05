package org.platformlayer.core.model;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;

import javax.xml.bind.annotation.XmlTransient;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.net.InetAddresses;

@XmlTransient
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

		for (EndpointInfo endpoint : Tag.PUBLIC_ENDPOINT.find(tags)) {
			endpoints.add(endpoint);
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
		return Tag.PUBLIC_ENDPOINT.build(this);
	}

	String getTagValue() {
		if (publicIp.contains(":")) {
			return "[" + publicIp + "]:" + port;
		} else {
			return publicIp + ":" + port;
		}
	}

	@Override
	public String toString() {
		return publicIp + ":" + port;
	}

	public InetAddress getAddress() {
		return InetAddresses.forString(publicIp);
	}

	public InetSocketAddress asSocketAddress() {
		if (port == null) {
			throw new IllegalArgumentException("port is not set");
		}
		return new InetSocketAddress(getAddress(), port);
	}

	public static EndpointInfo parseTagValue(String s) {
		int lastColon = s.lastIndexOf(':');
		if (lastColon == -1) {
			throw new IllegalStateException();
		}

		String portString = s.substring(lastColon + 1);
		String hostString = s.substring(0, lastColon);

		if (hostString.contains(":")) {
			if (!hostString.startsWith("[")) {
				throw new IllegalStateException();
			}
		}

		hostString = trimSquareBrackets(hostString);

		if (hostString.contains("[") || hostString.contains("]")) {
			throw new IllegalStateException();
		}

		EndpointInfo info = new EndpointInfo();
		info.publicIp = hostString;
		info.port = Integer.parseInt(portString);

		return info;
	}

	private static String trimSquareBrackets(String s) {
		while (s.startsWith("[") && s.endsWith("]")) {
			s = s.substring(1, s.length() - 1);
		}
		return s;
	}
}
