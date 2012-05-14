package org.platformlayer.service.cloud.direct.ops;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Properties;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.EndpointInfo;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.TagChanges;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsProvider;
import org.platformlayer.ops.OpsSystem;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.helpers.ImageFactory;
import org.platformlayer.ops.helpers.InstanceHelpers;
import org.platformlayer.ops.helpers.ServiceContext;
import org.platformlayer.ops.machines.PlatformLayerCloudMachine;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.pool.PoolBuilder;
import org.platformlayer.ops.pool.SocketAddressPoolAssignment;
import org.platformlayer.ops.pool.StaticFilesystemBackedPool;
import org.platformlayer.ops.tagger.Tagger;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.cloud.direct.model.DirectInstance;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class PublicPorts extends OpsTreeBase {
	static final Logger log = Logger.getLogger(PublicPorts.class);

	public int backendPort;
	public int publicPort;
	public DirectInstance backendItem;

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

	static class PublicAddressDynamicPool extends StaticFilesystemBackedPool {
		private final int publicPort;

		public PublicAddressDynamicPool(PoolBuilder poolBuilder, OpsTarget target, File resourceDir, File assignedDir,
				int publicPort) {
			super(poolBuilder, target, resourceDir, assignedDir);
			this.publicPort = publicPort;
		}

		@Override
		protected Iterable<String> pickRandomResource() throws OpsException {
			return Iterables.transform(super.pickRandomResource(), new Function<String, String>() {
				@Override
				public String apply(String input) {
					return input + "_" + publicPort;
				}
			});
			//
			// List<String> all = Lists.newArrayList();
			//
			// String textFile = target.readTextFile(resourceFile);
			// if (textFile == null) {
			// throw new OpsException("Resource file not found: " + resourceFile);
			// }
			// for (String line : textFile.split("\n")) {
			// line = line.trim();
			// if (line.isEmpty()) {
			// continue;
			// }
			// all.add(line + "_" + publicPort);
			// }
			//
			// Collections.shuffle(all);
			// return all;
		}

		@Override
		public Properties readProperties(String key) throws OpsException {
			String[] tokens = key.split("_");
			if (tokens.length != 2) {
				throw new OpsException("Invalid key format");
			}
			Properties properties = super.readProperties(tokens[0]);
			properties.put("port", tokens[1]);
			return properties;
		}

		@Override
		public String toString() {
			return getClass().getName() + ":" + resourceDir + ":" + publicPort;
		}
	};

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
			assignPublicAddress = injected(SocketAddressPoolAssignment.class);
			assignPublicAddress.holder = DirectCloudUtils.getInstanceDir(backendItem);
			assignPublicAddress.poolProvider = DirectCloudUtils.getPublicAddressPool4(publicPort);

			cloudHost.addChild(assignPublicAddress);
		}

		{
			ForwardPort forward = injected(ForwardPort.class);
			forward.publicAddress = assignPublicAddress;
			forward.uuid = uuid;

			forward.privateAddress = new OpsProvider<String>() {
				@Override
				public String get() throws OpsException {
					// Refresh item to pick up new tags
					backendItem = platformLayerClient.getItem(OpsSystem.toKey(backendItem), DirectInstance.class);

					PlatformLayerCloudMachine instanceMachine = (PlatformLayerCloudMachine) instanceHelpers
							.getMachine(backendItem);
					DirectInstance instance = (DirectInstance) instanceMachine.getInstance();
					return DirectCloudUtils.getNetworkAddress(instance);
				}
			};
			forward.privatePort = backendPort;

			cloudHost.addChild(forward);
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
				tagger.platformLayerKey = OpsSystem.toKey(tagItem);
				tagger.tagChangesProvider = tagChanges;
			}
		}
	}

}
