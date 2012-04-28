package org.platformlayer;

import java.util.ArrayList;
import java.util.List;

import org.platformlayer.core.model.Tag;
import org.platformlayer.core.model.Tags;

import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

public class EndpointInfo {

	public EndpointInfo() {
	}

	public EndpointInfo(String address, int publicPort) {
		this.publicIp = address;
		this.port = publicPort;
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

	public static EndpointInfo findEndpoint(Tags tags, Integer port) {
		for (EndpointInfo publicEndpoint : getEndpoints(tags)) {
			if (publicEndpoint.matches(port)) {
				return publicEndpoint;
			}
		}
		return null;
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

}
