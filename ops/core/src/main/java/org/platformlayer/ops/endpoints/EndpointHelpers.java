package org.platformlayer.ops.endpoints;

import java.util.ArrayList;
import java.util.List;

import org.platformlayer.core.model.Tag;
import org.platformlayer.core.model.Tags;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

public class EndpointHelpers {
    public List<EndpointInfo> getEndpoints(Tags tags) {
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

    public EndpointInfo findEndpoint(Tags tags, Integer port) {
        for (EndpointInfo publicEndpoint : getEndpoints(tags)) {
            if (publicEndpoint.matches(port)) {
                return publicEndpoint;
            }
        }
        return null;
    }
}
