package org.platformlayer.service.platformlayer.ops;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Map;

import javax.inject.Inject;

import org.openstack.client.OpenstackProperties;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.service.machines.openstack.v1.OpenstackCloud;

import com.google.common.collect.Maps;

public class SoftwareRepositoryHelpers {
    @Inject
    PlatformLayerHelpers platformLayer;

    public Path getMavenBasePath() throws OpsException {
        URI uri = URI.create("openstack:///");
        Map<String, String> env = Maps.newHashMap();

        OpenstackCloud cloud = null;

        for (OpenstackCloud candidate : platformLayer.listItems(OpenstackCloud.class)) {
            if (cloud == null) {
                cloud = candidate;
            } else {
                throw new UnsupportedOperationException();
            }
        }

        if (cloud == null) {
            throw new OpsException("Cannot find cloud storage");
        }

        env.put(OpenstackProperties.AUTH_URL, cloud.getEndpoint());
        env.put(OpenstackProperties.AUTH_USER, cloud.getUsername());
        env.put(OpenstackProperties.AUTH_SECRET, cloud.getPassword());
        env.put(OpenstackProperties.AUTH_TENANT, cloud.getTenant());

        try {
            FileSystem fs = FileSystems.newFileSystem(uri, env);
            String containerName = "/maven.openstack.org/snapshot";

            return fs.getPath(containerName);
        } catch (IOException e) {
            throw new OpsException("Error connecting to cloud filesystem", e);
        }
    }
}
