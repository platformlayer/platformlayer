package org.platformlayer.service.cloud.direct.ops;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.log4j.Logger;
import org.platformlayer.core.model.Tag;
import org.platformlayer.core.model.TagChanges;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsProvider;
import org.platformlayer.ops.OpsSystem;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.helpers.ImageFactory;
import org.platformlayer.ops.helpers.InstanceHelpers;
import org.platformlayer.ops.helpers.ServiceContext;
import org.platformlayer.ops.lxc.FilesystemBackedPool;
import org.platformlayer.ops.machines.PlatformLayerCloudMachine;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.tagger.Tagger;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.cloud.direct.model.DirectInstance;
import org.platformlayer.service.cloud.direct.ops.kvm.PoolAssignment;

import com.google.common.collect.Lists;

public class PublicPorts extends OpsTreeBase {
	static final Logger log = Logger.getLogger(PublicPorts.class);

	public int backendPort;
	public int publicPort;
	public DirectInstance backendItem;

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

	static class PublicAddressDynamicPool extends FilesystemBackedPool {
		private File resourceFile;
		private final int publicPort;

		public PublicAddressDynamicPool(OpsTarget target, File assignedDir, File resourceFile, int publicPort) {
			super(target, assignedDir);
			this.resourceFile = resourceFile;
			this.publicPort = publicPort;
		}

		@Override
		protected Iterable<String> pickRandomResource() throws OpsException {
			List<String> all = Lists.newArrayList();

			String textFile = target.readTextFile(resourceFile);
			if (textFile == null) {
				throw new OpsException("Resource file not found: " + resourceFile);
			}
			for (String line : textFile.split("\n")) {
				line = line.trim();
				if (line.isEmpty()) {
					continue;
				}
				all.add(line + "_" + publicPort);
			}

			Collections.shuffle(all);
			return all;
		}

		@Override
		public Properties readProperties(String key) throws OpsException {
			Properties properties = new Properties();
			String[] tokens = key.split("_");
			if (tokens.length != 2) {
				throw new OpsException("Invalid key format");
			}
			properties.put("address", tokens[0]);
			properties.put("port", tokens[1]);
			return properties;
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

		final PoolAssignment assignPublicAddress;
		{
			assignPublicAddress = injected(PoolAssignment.class);
			assignPublicAddress.holder = DirectCloudUtils.getInstanceDir(backendItem);

			assignPublicAddress.poolProvider = new Provider<FilesystemBackedPool>() {

				@Override
				public FilesystemBackedPool get() {
					OpsTarget target = OpsContext.get().getInstance(OpsTarget.class);

					File poolPath = DirectCloudUtils.getPoolPath("network-public");

					File assignedDir = new File(poolPath, "assigned");

					// We have a single file containing the addresses; we effectively have another pool for each port
					File resourceFile = new File(poolPath, "all");

					return new PublicAddressDynamicPool(target, assignedDir, resourceFile, publicPort);
				}
			};

			cloudHost.addChild(assignPublicAddress);
		}

		{
			ForwardPort forward = injected(ForwardPort.class);
			forward.publicAddress = OpsProvider.getProperty(assignPublicAddress, "address");
			forward.publicPort = publicPort;
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
			Tagger tagger = injected(Tagger.class);

			OpsProvider<TagChanges> tagChanges = new OpsProvider<TagChanges>() {
				@Override
				public TagChanges get() {
					TagChanges tagChanges = new TagChanges();
					String address = assignPublicAddress.getAssigned().getProperty("address");
					tagChanges.addTags.add(new Tag(Tag.PUBLIC_ENDPOINT, address + ":" + publicPort));
					return tagChanges;
				}
			};
			tagger.platformLayerKey = OpsSystem.toKey(backendItem);
			tagger.tagChangesProvider = tagChanges;

			cloudHost.addChild(tagger);
		}
	}

}
