package org.platformlayer.service.cloud.direct.ops;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Properties;

import javax.inject.Inject;

import org.platformlayer.InetAddressChooser;
import org.platformlayer.core.model.EndpointInfo;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.Tag;
import org.platformlayer.core.model.TagChanges;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsProvider;
import org.platformlayer.ops.firewall.Protocol;
import org.platformlayer.ops.firewall.Transport;
import org.platformlayer.ops.firewall.scripts.IptablesForwardPort;
import org.platformlayer.ops.helpers.ImageFactory;
import org.platformlayer.ops.helpers.InstanceHelpers;
import org.platformlayer.ops.helpers.ServiceContext;
import org.platformlayer.ops.machines.InetAddressUtils;
import org.platformlayer.ops.machines.PlatformLayerCloudMachine;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.pool.DelegatingResourcePool;
import org.platformlayer.ops.pool.ResourcePool;
import org.platformlayer.ops.pool.SocketAddressPoolAssignment;
import org.platformlayer.ops.tagger.Tagger;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.cloud.direct.model.DirectInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.inject.Provider;

public class PublicPorts extends OpsTreeBase {

	private static final Logger log = LoggerFactory.getLogger(PublicPorts.class);

	public int backendPort;
	public int publicPort;
	public DirectInstance backendItem;

	public Transport transport;

	public String uuid;

	// TODO: Only tag the endpoint, and then copy that to the instance to give sequencing
	public List<ItemBase> tagItems = Lists.newArrayList();

	@Handler
	public void handler() throws OpsException, IOException {
	}

	@Inject
	ImageFactory imageFactory;

	@Inject
	PlatformLayerHelpers platformLayerClient;

	@Inject
	InstanceHelpers instanceHelpers;

	@Inject
	ServiceContext service;

	@Override
	protected void addChildren() throws OpsException {
		final CloudInstanceMapper cloudHost;
		{
			cloudHost = injected(CloudInstanceMapper.class);
			cloudHost.createInstance = false;
			cloudHost.instance = backendItem;
			addChild(cloudHost);
		}

		final SocketAddressPoolAssignment assignPublicAddress;
		{
			assignPublicAddress = cloudHost.addChild(SocketAddressPoolAssignment.class);
			assignPublicAddress.holder = DirectCloudUtils.getInstanceDir(backendItem);

			if (Objects.equal(transport, Transport.Ipv6)) {
				assignPublicAddress.poolProvider = new Provider<ResourcePool>() {
					@Override
					public ResourcePool get() {
						final ResourcePool pool = DirectCloudUtils.getAddressPool6().get();

						return new DelegatingResourcePool(pool) {
							@Override
							public Properties readProperties(String key) throws OpsException {
								Properties properties = super.readProperties(key);
								properties.setProperty("port", "" + publicPort);
								return properties;
							}
						};
					}
				};
			} else {
				assignPublicAddress.poolProvider = DirectCloudUtils.getPublicAddressPool4(publicPort);
			}
		}

		if (Objects.equal(transport, Transport.Ipv6)) {
			// TODO: Do we need separate frontend / backend ports really?
			if (this.publicPort != this.backendPort) {
				throw new UnsupportedOperationException();
			}
		} else {
			for (Protocol protocol : Protocol.TcpAndUdp()) {
				IptablesForwardPort forward = injected(IptablesForwardPort.class);
				forward.publicAddress = assignPublicAddress;
				forward.ruleKey = protocol.name() + "-" + uuid;
				forward.protocol = protocol;

				forward.privateAddress = new OpsProvider<String>() {
					@Override
					public String get() throws OpsException {
						// Refresh item to pick up new tags
						backendItem = platformLayerClient.getItem(backendItem.getKey(), DirectInstance.class);

						PlatformLayerCloudMachine instanceMachine = (PlatformLayerCloudMachine) instanceHelpers
								.getMachine(backendItem);
						DirectInstance instance = (DirectInstance) instanceMachine.getInstance();
						List<InetAddress> addresses = Tag.NETWORK_ADDRESS.find(instance);
						InetAddress address = InetAddressChooser.preferIpv4().choose(addresses);
						if (address == null) {
							throw new IllegalStateException();
						}

						if (InetAddressUtils.isIpv6(address)) {
							// We can't NAT IPV4 -> IPV6 (I think)
							throw new IllegalStateException();
						}
						return address.getHostAddress();
					}
				};
				forward.privatePort = backendPort;

				cloudHost.addChild(forward);
			}
		}

		{
			OpsProvider<TagChanges> tagChanges = new OpsProvider<TagChanges>() {
				@Override
				public TagChanges get() {
					TagChanges tagChanges = new TagChanges();

					InetSocketAddress socketAddress = assignPublicAddress.get();
					if (socketAddress.getPort() != publicPort) {
						throw new IllegalStateException();
					}

					EndpointInfo endpoint = new EndpointInfo(socketAddress);
					tagChanges.addTags.add(endpoint.toTag());
					return tagChanges;
				}
			};

			for (ItemBase tagItem : tagItems) {
				Tagger tagger = addChild(Tagger.class);
				tagger.platformLayerKey = tagItem.getKey();
				tagger.tagChangesProvider = tagChanges;
			}
		}
	}

}
