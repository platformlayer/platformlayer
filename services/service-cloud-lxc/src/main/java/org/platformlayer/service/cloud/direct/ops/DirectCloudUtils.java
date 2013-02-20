package org.platformlayer.service.cloud.direct.ops;

import java.io.File;
import java.net.InetSocketAddress;

import javax.inject.Inject;

import org.platformlayer.core.model.AddressModel;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ops.Machine;
import org.platformlayer.ops.OpaqueMachine;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsProvider;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.helpers.ServiceContext;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.networks.IpRange;
import org.platformlayer.ops.networks.NetworkPoint;
import org.platformlayer.ops.pool.NetworkPoolBuilder;
import org.platformlayer.ops.pool.PoolBuilder;
import org.platformlayer.ops.pool.ResourcePool;
import org.platformlayer.ops.pool.StaticFilesystemBackedPool;
import org.platformlayer.service.cloud.direct.model.DirectCloud;
import org.platformlayer.service.cloud.direct.model.DirectHost;
import org.platformlayer.service.cloud.direct.model.DirectInstance;
import org.platformlayer.service.cloud.direct.model.DirectNetwork;
import org.platformlayer.service.cloud.direct.ops.network.PlatformlayerBackedPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

public class DirectCloudUtils {
	static final Logger log = LoggerFactory.getLogger(DirectCloudUtils.class);

	static File POOL_ROOT = new File("/var/pools/");

	public static final File LXC_BASE_DIR = new File("/var/lib/lxc");
	public static final File KVM_BASE_DIR = new File("/var/kvm");

	@Inject
	ServiceContext service;

	@Inject
	PlatformLayerHelpers platformLayer;

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

	private <T> OpsProvider<ResourcePool<T>> getNetworkPoolProvider(final Class<T> clazz, final String key,
			final OpsProvider<? extends PoolBuilder<T>> poolBuilderProvider) {
		return new OpsProvider<ResourcePool<T>>() {
			@Override
			public ResourcePool<T> get() throws OpsException {
				PlatformLayerKey sharedResourcePoolKey = getSharedNetworkKey();
				if (sharedResourcePoolKey == null) {
					return getUnsharedPrivatePoolProvider(clazz, key, poolBuilderProvider).get();
				} else {
					return getSharedPoolProvider(clazz, sharedResourcePoolKey, key, poolBuilderProvider).get();
				}
			}
		};

	}

	private <T> OpsProvider<ResourcePool<T>> getSharedPoolProvider(final Class<T> childType,
			final PlatformLayerKey resourcePoolKey, final String key,
			final OpsProvider<? extends PoolBuilder<T>> poolBuilderProvider) {
		return new OpsProvider<ResourcePool<T>>() {
			@Override
			public ResourcePool<T> get() throws OpsException {
				// OpsTarget target = OpsContext.get().getInstance(OpsTarget.class);
				//
				// File poolPath = getPoolPath(key);
				//
				// File resourceDir = new File(poolPath, "all");
				// File assignedDir = new File(poolPath, "assigned");

				PoolBuilder<T> poolBuilder = null;
				if (poolBuilderProvider != null) {
					poolBuilder = poolBuilderProvider.get();
				}

				return new PlatformlayerBackedPool<T>(platformLayer, resourcePoolKey, childType, poolBuilder);
			}
		};
	}

	private static <T> OpsProvider<ResourcePool<T>> getUnsharedPrivatePoolProvider(final Class<T> clazz,
			final String key, final OpsProvider<? extends PoolBuilder<T>> poolBuilderProvider) {
		return new OpsProvider<ResourcePool<T>>() {
			@Override
			public ResourcePool<T> get() throws OpsException {
				OpsTarget target = OpsContext.get().getInstance(OpsTarget.class);

				File poolPath = getPoolPath(key);

				File resourceDir = new File(poolPath, "all");
				File assignedDir = new File(poolPath, "assigned");

				PoolBuilder<T> poolBuilder = null;
				if (poolBuilderProvider != null) {
					poolBuilder = poolBuilderProvider.get();
				}
				return new StaticFilesystemBackedPool<T>(clazz, poolBuilder, target, resourceDir, assignedDir);
			}
		};
	}

	private static PlatformLayerKey getSharedNetworkKey() {
		DirectCloud cloud = OpsContext.get().getInstance(DirectCloud.class);
		DirectHost host = OpsContext.get().getInstance(DirectHost.class);

		PlatformLayerKey sharedNetwork = host.network;
		if (sharedNetwork == null) {
			sharedNetwork = cloud.network;
		}

		return sharedNetwork;
	}

	public OpsProvider<ResourcePool<InetSocketAddress>> getPublicAddressPool4(final int publicPort) {
		OpsProvider<ResourcePool<InetSocketAddress>> pool = new OpsProvider<ResourcePool<InetSocketAddress>>() {
			@Override
			public ResourcePool<InetSocketAddress> get() throws OpsException {
				DirectHost host = OpsContext.get().getInstance(DirectHost.class);
				OpsTarget target = OpsContext.get().getInstance(OpsTarget.class);

				PlatformLayerKey sharedNetworkKey = getSharedNetworkKey();

				// We don't skip here, at the moment.
				// We may just need a comma separated list in future...
				int skipCount = 0;

				if (sharedNetworkKey == null) {
					File poolPath = getPoolPath("sockets-ipv4-public");

					File resourceDir = new File(poolPath, "all");
					File assignedBase = new File(poolPath, "assigned");
					File perPortDir = new File(assignedBase, "port" + publicPort);

					PoolBuilder<AddressModel> poolBuilder = null;

					String ipv4Public = host.ipv4Public;
					if (ipv4Public != null) {
						poolBuilder = new NetworkPoolBuilder(ipv4Public, skipCount);
					}

					StaticFilesystemBackedPool<AddressModel> addressPool = new StaticFilesystemBackedPool<AddressModel>(
							AddressModel.class, poolBuilder, target, resourceDir, perPortDir);

					return new AssignPortToAddressPool(addressPool, publicPort);
				} else {
					// We can't have different hosts answering the same IP on different ports
					// (well, not really)

					throw new UnsupportedOperationException();

					// DirectNetwork network = platformLayer.getItem(sharedNetworkKey, DirectNetwork.class);
					//
					// for (AddressModel net : network.getNetworks()) {
					// if (Strings.isNullOrEmpty(net.cidr)) {
					// continue;
					// }
					//
					// IpRange cidr = IpRange.parse(net.cidr);
					// if (!cidr.isIpv4()) {
					// continue;
					// }
					//
					// NetworkPoolBuilder poolBuilder = new NetworkPoolBuilder(net.cidr, skipCount, net);
					// PlatformlayerBackedPool<AddressModel> addressPool = new PlatformlayerBackedPool<AddressModel>(
					// platformLayer, sharedNetworkKey, AddressModel.class, poolBuilder);
					// return new AssignPortToAddressPool(addressPool, publicPort);
					// }
					//
					// log.warn("Unable to find an IPV4 network configured on " + sharedNetworkKey);
					// return null;
				}
			}
		};

		return pool;
	}

	public static OpsProvider<ResourcePool<AddressModel>> getPrivateAddressPool4() {
		OpsProvider<PoolBuilder<AddressModel>> poolBuilder = new OpsProvider<PoolBuilder<AddressModel>>() {
			@Override
			public PoolBuilder<AddressModel> get() {
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

		return getUnsharedPrivatePoolProvider(AddressModel.class, "addresses-ipv4-private", poolBuilder);
	}

	public OpsProvider<ResourcePool<AddressModel>> getAddressPool6() {
		OpsProvider<PoolBuilder<AddressModel>> poolBuilder = new OpsProvider<PoolBuilder<AddressModel>>() {
			@Override
			public PoolBuilder<AddressModel> get() throws OpsException {
				DirectHost host = OpsContext.get().getInstance(DirectHost.class);

				PlatformLayerKey sharedNetworkKey = getSharedNetworkKey();

				// Skip the first entries in the CIDR as it's probably not valid
				// 0: Network identifier
				// 1: Gateway
				// 2: Host
				int skipCount = 3;

				if (sharedNetworkKey != null) {
					DirectNetwork network = platformLayer.getItem(sharedNetworkKey, DirectNetwork.class);

					for (AddressModel net : network.getNetworks()) {
						if (Strings.isNullOrEmpty(net.cidr)) {
							continue;
						}

						IpRange cidr = IpRange.parse(net.cidr);
						if (!cidr.isIpv6()) {
							continue;
						}

						return new NetworkPoolBuilder(net.cidr, skipCount, net);
					}

					log.warn("Unable to find an IPV6 network configured on " + sharedNetworkKey);
					return null;
				} else {
					String privateCidr = host.ipv6;
					if (privateCidr != null) {
						return new NetworkPoolBuilder(privateCidr, skipCount, null);
					}
					return null;
				}
			}
		};

		return getNetworkPoolProvider(AddressModel.class, "addresses-ipv6", poolBuilder);
	}

	public static OpsProvider<ResourcePool<InetSocketAddress>> getKvmMonitorPortPool() {
		return getUnsharedPrivatePoolProvider(InetSocketAddress.class, "kvm-monitor",
				OpsProvider.of(new KvmMonitorPoolBuilder()));
	}

	public static OpsProvider<ResourcePool<InetSocketAddress>> getVncPortPool() {
		return getUnsharedPrivatePoolProvider(InetSocketAddress.class, "kvm-vnc",
				OpsProvider.of(new VncPortPoolBuilder()));
	}

}
