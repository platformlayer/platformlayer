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
import org.platformlayer.ops.pool.NetworkPoolBuilder;
import org.platformlayer.ops.pool.PoolBuilder;
import org.platformlayer.ops.pool.ResourcePool;
import org.platformlayer.ops.pool.StaticFilesystemBackedPool;
import org.platformlayer.service.cloud.direct.model.DirectHost;
import org.platformlayer.service.cloud.direct.model.DirectInstance;
import org.platformlayer.service.cloud.direct.ops.PublicPorts.PublicAddressDynamicPool;

import com.google.inject.util.Providers;

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

	private static File getPoolPath(String key) {
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

	private static Provider<ResourcePool> getPoolProvider(final String key,
			final Provider<? extends PoolBuilder> poolBuilderProvider) {
		return new Provider<ResourcePool>() {
			@Override
			public FilesystemBackedPool get() {
				OpsTarget target = OpsContext.get().getInstance(OpsTarget.class);

				File poolPath = getPoolPath(key);

				File resourceDir = new File(poolPath, "all");
				File assignedDir = new File(poolPath, "assigned");

				PoolBuilder poolBuilder = null;
				if (poolBuilderProvider != null) {
					poolBuilder = poolBuilderProvider.get();
				}
				return new StaticFilesystemBackedPool(poolBuilder, target, resourceDir, assignedDir);
			}
		};
	}

	public static String getNetworkAddress(DirectInstance instance) {
		Tags tags = instance.getTags();
		return tags.findUnique(Tag.NETWORK_ADDRESS);
	}

	public static Provider<ResourcePool> getPublicAddressPool4(final int publicPort) {
		return new Provider<ResourcePool>() {
			@Override
			public FilesystemBackedPool get() {
				OpsTarget target = OpsContext.get().getInstance(OpsTarget.class);

				File poolPath = getPoolPath("sockets-ipv4-public");

				File resourceDir = new File(poolPath, "all");
				File assignedDir = new File(poolPath, "assigned");

				PoolBuilder poolBuilder = null;

				DirectHost host = OpsContext.get().getInstance(DirectHost.class);
				String ipv4Public = host.ipv4Public;
				if (ipv4Public != null) {
					// We don't skip here, .sat the moment.
					// We may just need a comma separated list in future...
					int skipCount = 0;
					poolBuilder = new NetworkPoolBuilder(ipv4Public, skipCount);
				}

				return new PublicAddressDynamicPool(poolBuilder, target, resourceDir, assignedDir, publicPort);
			}
		};
	}

	public static Provider<ResourcePool> getPrivateAddressPool4() {
		Provider<PoolBuilder> poolBuilder = new Provider<PoolBuilder>() {
			@Override
			public PoolBuilder get() {
				DirectHost host = OpsContext.get().getInstance(DirectHost.class);
				String privateCidr = host.ipv4Private;
				if (privateCidr != null) {
					// Skip the first entries in the CIDR as it's probably not valid
					// 0: Network identifier
					// 1: Gateway (?)
					// 2: Host (?)
					int skipCount = 3;
					return new NetworkPoolBuilder(privateCidr, skipCount);
				}
				return null;
			}
		};

		return getPoolProvider("addresses-ipv4-private", poolBuilder);
	}

	public static Provider<ResourcePool> getAddressPool6() {
		Provider<PoolBuilder> poolBuilder = new Provider<PoolBuilder>() {
			@Override
			public PoolBuilder get() {
				DirectHost host = OpsContext.get().getInstance(DirectHost.class);
				String privateCidr = host.ipv6;
				if (privateCidr != null) {
					// Skip the first entries in the CIDR as it's probably not valid
					// 0: Network identifier
					// 1: Gateway
					// 2: Host
					int skipCount = 3;
					return new NetworkPoolBuilder(privateCidr, skipCount);
				}
				return null;
			}
		};

		return getPoolProvider("addresses-ipv6", poolBuilder);
	}

	public static Provider<ResourcePool> getKvmMonitorPortPool() {
		return getPoolProvider("kvm-monitor", Providers.of(new KvmMonitorPoolBuilder()));
	}

	public static Provider<ResourcePool> getVncPortPool() {
		return getPoolProvider("kvm-vnc", Providers.of(new VncPortPoolBuilder()));
	}

}
