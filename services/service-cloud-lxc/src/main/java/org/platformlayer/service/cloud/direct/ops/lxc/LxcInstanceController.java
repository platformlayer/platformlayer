package org.platformlayer.service.cloud.direct.ops.lxc;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import javax.inject.Inject;
import javax.inject.Provider;

import org.platformlayer.PlatformLayerClient;
import org.platformlayer.core.model.AddressModel;
import org.platformlayer.core.model.Tag;
import org.platformlayer.core.model.TagChanges;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsProvider;
import org.platformlayer.ops.filesystem.ManagedDirectory;
import org.platformlayer.ops.helpers.InstanceHelpers;
import org.platformlayer.ops.helpers.ServiceContext;
import org.platformlayer.ops.helpers.SshKeys;
import org.platformlayer.ops.images.ImageFormat;
import org.platformlayer.ops.instances.ImageFactory;
import org.platformlayer.ops.pool.NetworkAddressPoolAssignment;
import org.platformlayer.ops.supervisor.StandardService;
import org.platformlayer.ops.tagger.Tagger;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.cloud.direct.model.DirectInstance;
import org.platformlayer.service.cloud.direct.ops.CloudInstanceMapper;
import org.platformlayer.service.cloud.direct.ops.DirectCloudUtils;
import org.platformlayer.service.cloud.direct.ops.DirectHostController;
import org.platformlayer.service.cloud.direct.ops.DownloadImage;
import org.platformlayer.service.cloud.direct.ops.InstanceScript;
import org.platformlayer.service.cloud.direct.ops.cloud.CloudMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fathomdb.crypto.OpenSshUtils;

public class LxcInstanceController extends OpsTreeBase {

	private static final Logger log = LoggerFactory.getLogger(LxcInstanceController.class);

	public File instanceDir;
	public String id;
	public int minimumMemoryMB;

	@Inject
	DirectCloudUtils directCloudHelpers;

	@Inject
	PlatformLayerClient platformLayer;

	@Inject
	ImageFactory imageFactory;

	@Inject
	ServiceContext service;

	@Inject
	InstanceHelpers instances;

	@Inject
	CloudMap cloudMap;

	@Inject
	ImageFactory images;

	@Inject
	PlatformLayerClient platformLayerClient;

	@Inject
	SshKeys sshKeys;

	@Handler
	public void handler() throws OpsException, IOException {
	}

	@Override
	protected void addChildren() throws OpsException {
		final DirectInstance model = OpsContext.get().getInstance(DirectInstance.class);

		CloudInstanceMapper instance;
		{
			instance = injected(CloudInstanceMapper.class);
			instance.instance = OpsContext.get().getInstance(DirectInstance.class);
			addChild(instance);
		}

		instance.addChild(ManagedDirectory.build(getInstanceDir(), "700"));

		// TODO: If we're not going to assign an IPV4 redirect, we might not need this
		final Provider<AddressModel> address4;
		{
			NetworkAddressPoolAssignment provider = instance.addChild(NetworkAddressPoolAssignment.class);
			provider.holder = model.getKey();
			provider.poolProvider = DirectCloudUtils.getPrivateAddressPool4();

			address4 = provider;
		}

		final Provider<AddressModel> address6;
		{
			NetworkAddressPoolAssignment provider = instance.addChild(NetworkAddressPoolAssignment.class);
			provider.holder = model.getKey();
			provider.poolProvider = directCloudHelpers.getAddressPool6();

			address6 = provider;
		}

		// {
		// NetworkTunDevice tun = injected(NetworkTunDevice.class);
		// tun.interfaceName = getEthernetDeviceName();
		// tun.bridgeName = Providers.getProperty(assignNetworkAddress, "bridge");
		// instance.addChild(tun);
		// }

		{
			DownloadImage download = injected(DownloadImage.class);
			download.imageFile = new File(getInstanceDir(), "rootfs");
			download.recipeKey = model.recipeId;
			download.imageFormats = Collections.singletonList(ImageFormat.Tar);
			instance.addChild(download);
		}

		{
			LxcBootstrap bootstrap = injected(LxcBootstrap.class);
			bootstrap.address4 = address4;
			bootstrap.address6 = address6;
			bootstrap.lxcId = id;
			bootstrap.instanceDir = instanceDir;

			try {
				bootstrap.sshPublicKey = OpenSshUtils.readSshPublicKey(model.sshPublicKey);
			} catch (IOException e) {
				throw new OpsException("Error deserializing SSH key", e);
			}

			bootstrap.hostname = model.hostname;

			instance.addChild(bootstrap);
		}

		InstanceScript script;
		{
			script = instance.addChild(InstanceScript.class);
			script.filePath = new File(DirectHostController.LXC_INSTANCE_DIR, id);

			String key = "lxc-" + id;
			script.key = key;

			script.addresses.add(address4);
			script.addresses.add(address6);

			// script.hostPrimaryInterface = hostModel.publicInterface;

			Command command = Command.build("lxc-start");
			command.addLiteral("--name").addQuoted(id);
			script.launchInstanceCommand = command;
		}

		{
			// ManagedSupervisordInstance service = instance.addChild(ManagedSupervisordInstance.class);
			StandardService service = instance.addChild(StandardService.class);
			script.configure(model, service);
		}

		{
			OpsProvider<TagChanges> tagChanges = new OpsProvider<TagChanges>() {
				@Override
				public TagChanges get() {
					TagChanges tagChanges = new TagChanges();

					tagChanges.addTags.add(Tag.INSTANCE_KEY.build(model.getKey()));

					AddressModel ipv4 = address4.get();
					AddressModel ipv6 = address6.get();

					tagChanges.addTags.add(ipv4.toTag());
					tagChanges.addTags.add(ipv6.toTag());

					return tagChanges;
				}
			};

			instance.addChild(Tagger.build(model, tagChanges));
		}
	}

	private File getInstanceDir() {
		return instanceDir;
	}
}
