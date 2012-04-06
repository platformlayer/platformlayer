package org.platformlayer.service.cloud.direct.ops;

import java.io.File;

import javax.inject.Provider;

import org.platformlayer.core.model.Tag;
import org.platformlayer.core.model.Tags;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.lxc.FilesystemBackedPool;
import org.platformlayer.ops.lxc.StaticFilesystemBackedPool;
import org.platformlayer.service.cloud.direct.model.DirectInstance;

public class DirectCloudUtils {
    static File POOL_ROOT = new File("/var/pools/");

    public static final File LXC_BASE_DIR = new File("/var/lib/lxc");
    public static final File KVM_BASE_DIR = new File("/var/kvm");

    public static File getPoolPath(String key) {
        File poolPath = new File(POOL_ROOT, key);
        return poolPath;
    }

    public static File getInstanceDir(DirectInstance directInstance) {
        String id = directInstance.getId();

        if (directInstance.hostPolicy.allowRunInContainer) {
            return new File(LXC_BASE_DIR, id);
        } else {
            return new File(KVM_BASE_DIR, id);
        }
    }

    public static Provider<FilesystemBackedPool> getPoolProvider(final String key) {
        return new Provider<FilesystemBackedPool>() {
            @Override
            public FilesystemBackedPool get() {
                OpsTarget target = OpsContext.get().getInstance(OpsTarget.class);

                File poolPath = getPoolPath(key);

                File resourceDir = new File(poolPath, "all");
                File assignedDir = new File(poolPath, "assigned");

                return new StaticFilesystemBackedPool(target, resourceDir, assignedDir);
            }
        };
    }

    public static String getNetworkAddress(DirectInstance instance) {
        Tags tags = instance.getTags();
        return tags.findUnique(Tag.NETWORK_ADDRESS);
    }
}
