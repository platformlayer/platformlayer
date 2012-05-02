package org.platformlayer.service.cloud.direct.ops;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Provider;

import org.platformlayer.core.model.Tag;
import org.platformlayer.core.model.Tags;
import org.platformlayer.ops.Machine;
import org.platformlayer.ops.OpaqueMachine;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.helpers.ServiceContext;
import org.platformlayer.ops.networks.NetworkPoint;
import org.platformlayer.ops.pool.FilesystemBackedPool;
import org.platformlayer.ops.pool.StaticFilesystemBackedPool;
import org.platformlayer.service.cloud.direct.model.DirectHost;
import org.platformlayer.service.cloud.direct.model.DirectInstance;

public class DirectCloudUtils {
	static File POOL_ROOT = new File("/var/pools/");

	public static final File LXC_BASE_DIR = new File("/var/lib/lxc");
	public static final File KVM_BASE_DIR = new File("/var/kvm");

	@Inject
	ServiceContext service;

	public OpsTarget toTarget(DirectHost host) throws OpsException {
		NetworkPoint address = NetworkPoint.forPublicHostname(host.host);
		Machine hostMachine = new OpaqueMachine(address);
		OpsTarget hostTarget = hostMachine.getTarget(service.getSshKey());
		return hostTarget;
	}

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

	private static Provider<FilesystemBackedPool> getPoolProvider(final String key) {
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

	public static Provider<FilesystemBackedPool> getPrivateAddressPool() {
		return getPoolProvider("network-private");
	}

	public static Provider<FilesystemBackedPool> getKvmMonitorPortPool() {
		return getPoolProvider("kvm-monitor");
	}

	public static Provider<FilesystemBackedPool> getVncPortPool() {
		return getPoolProvider("kvm-vnc");
	}

}
