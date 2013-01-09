package org.platformlayer.service.cloud.google.ops;

import java.io.IOException;
import java.security.PublicKey;

import javax.inject.Inject;

import org.platformlayer.core.model.Tag;
import org.platformlayer.core.model.TagChanges;
import org.platformlayer.crypto.OpenSshUtils;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsProvider;
import org.platformlayer.ops.bootstrap.ConfigureSshd;
import org.platformlayer.ops.helpers.ImageFactory;
import org.platformlayer.ops.ssh.SshAuthorizedKey;
import org.platformlayer.ops.tagger.Tagger;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.cloud.google.model.GoogleCloudInstance;
import org.platformlayer.service.cloud.google.ops.compute.GoogleComputeMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GoogleCloudInstanceController extends OpsTreeBase {

	private static final Logger log = LoggerFactory.getLogger(GoogleCloudInstanceController.class);

	@Handler
	public void handler() throws OpsException, IOException {
	}

	@Inject
	ImageFactory imageFactory;

	@Override
	protected void addChildren() throws OpsException {
		final GoogleCloudInstance model = OpsContext.get().getInstance(GoogleCloudInstance.class);

		PublicKey rootPublicKey;

		try {
			rootPublicKey = OpenSshUtils.readSshPublicKey(model.sshPublicKey);
		} catch (IOException e) {
			throw new OpsException("Cannot read SSH key");
		}

		CloudInstanceMapper instance;
		{
			instance = injected(CloudInstanceMapper.class);
			instance.instance = model;
			addChild(instance);
		}

		{
			SshAuthorizedKey authorizeRoot = instance.addChild(SshAuthorizedKey.class);
			authorizeRoot.user = "root";
			authorizeRoot.publicKey = rootPublicKey;
		}

		{
			instance.addChild(ConfigureSshd.class);
		}

		{
			OpsProvider<TagChanges> tagChanges = new OpsProvider<TagChanges>() {
				@Override
				public TagChanges get() {
					GoogleComputeMachine machine = OpsContext.get().getInstance(GoogleComputeMachine.class);

					TagChanges tagChanges = new TagChanges();
					tagChanges.addTags.add(Tag.INSTANCE_KEY.build(model.getKey()));
					tagChanges.addTags.addAll(machine.buildAddressTags());

					return tagChanges;
				}
			};

			instance.addChild(Tagger.build(model, tagChanges));
		}

		// Note: We can't bootstrap an instance, because we can't log in to it,
		// because the public key is not our service's public key

		// if (model.publicPorts != null) {
		// for (int publicPort : model.publicPorts) {
		// PublicPorts publicPortForward = injected(PublicPorts.class);
		// publicPortForward.port = publicPort;
		// publicPortForward.backendItem = model;
		// kvm.addChild(publicPortForward);
		// }
		// }
	}

}
