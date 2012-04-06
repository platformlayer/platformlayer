package org.platformlayer.service.cloud.openstack.ops.openstack;

import java.util.List;
import java.util.Map;

import org.openstack.client.common.OpenstackComputeClient;
import org.openstack.model.common.Extension;
import org.openstack.model.compute.Addresses.Network.Ip;
import org.platformlayer.ops.OpsException;
import org.platformlayer.service.cloud.openstack.model.OpenstackCloud;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * We should determine capabilities by trying it, rather than by hard-coding. For now, a lot of this stuff is in beta,
 * so it doesn't make sense to do, so hide the messiness in here!
 */
public class CloudBehaviours {
    private final OpenstackCloud cloud;

    public CloudBehaviours(OpenstackCloud cloud) {
        this.cloud = cloud;
    }

    class CachedInfo {
        private final List<Extension> extensions;

        public CachedInfo(List<Extension> extensions) {
            this.extensions = extensions;
        }

        public boolean supportsExtension(String namespace) {
            for (Extension extension : extensions) {
                if (namespace.equals(extension.getNamespace()))
                    return true;
            }
            return false;
        }
    }

    static final Map<String, CachedInfo> cachedInfo = Maps.newHashMap();

    CachedInfo getCachedInfo() throws OpsException {
        // TODO: We key on endpoint, to avoid repeated calls.
        // Is it safe to assume that everyone has the same capabilities on a cloud??
        CachedInfo info = cachedInfo.get(cloud.endpoint);
        if (info == null) {
            OpenstackCloudHelpers helpers = new OpenstackCloudHelpers();
            OpenstackComputeClient compute = helpers.buildOpenstackComputeClient(cloud);
            List<Extension> extensions = Lists.newArrayList(compute.root().extensions().list());
            info = new CachedInfo(extensions);
            cachedInfo.put(cloud.endpoint, info);
        }
        return info;
    }

    public boolean publicIpsReportedAsPrivate() {
        if (isHpCloud())
            return true;

        if (isDevstackPrivateCloud())
            return true;

        return false;
    }

    public boolean isHpCloud() {
        String authenticationUrl = cloud.endpoint;
        return authenticationUrl.contains("hpcloudsvc.com");
    }

    public boolean isRackspaceCloud() {
        String authenticationUrl = cloud.endpoint;
        return authenticationUrl.contains("rackspacecloud.com");
    }

    public boolean isDevstackPrivateCloud() {
        // TODO: Hack
        String authenticationUrl = cloud.endpoint;
        return authenticationUrl.contains("192.168.");
    }

    public boolean isPublic(Ip ip) {
        if (isDevstackPrivateCloud()) {
            // TODO: Hack
            return true;
        }

        if ("4".equals(ip.getVersion())) {
            String addr = ip.getAddr();
            if (addr.startsWith("10."))
                return false;
            if (addr.startsWith("192.168."))
                return false;
            for (int i = 16; i <= 31; i++) {
                if (addr.startsWith("172." + i + "."))
                    return false;
            }
            return true;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public boolean canUploadImages() {
        if (isHpCloud()) {
            return false;
        }

        if (isDevstackPrivateCloud()) {
            // Let's keep testing our ability to build images!
            return true;
        }

        // It's generally faster not to build / upload images!
        return false;
        // return true;
    }

    public boolean supportsFileInjection() {
        if (isHpCloud()) {
            return false;
        }

        if (isRackspaceCloud()) {
            return true;
        }

        // It's probably better not to use file injection...
        return false;
        // return true;
    }

    public boolean useConfigDrive() {
        if (isHpCloud()) {
            return false;
        }

        if (isRackspaceCloud()) {
            return false;
        }

        return true;
    }

    public boolean supportsPublicKeys() throws OpsException {
        return supportsExtension("http://docs.openstack.org/ext/keypairs/api/v1.1") || supportsExtension("http://docs.openstack.org/compute/ext/keypairs/api/v1.1");
    }

    public boolean supportsSecurityGroups() throws OpsException {
        return supportsExtension("http://docs.openstack.org/ext/securitygroups/api/v1.1") || supportsExtension("http://docs.openstack.org/compute/ext/securitygroups/api/v1.1");
    }

    private boolean supportsExtension(String namespace) throws OpsException {
        return getCachedInfo().supportsExtension(namespace);
    }
}
